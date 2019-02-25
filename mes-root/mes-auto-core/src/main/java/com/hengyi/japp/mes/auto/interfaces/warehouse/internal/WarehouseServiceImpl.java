package com.hengyi.japp.mes.auto.interfaces.warehouse.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkCarRuntimeService;
import com.hengyi.japp.mes.auto.application.event.PackageBoxFlipEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxFlipType;
import com.hengyi.japp.mes.auto.dto.EntityByCodeDTO;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.exception.MultiBatchException;
import com.hengyi.japp.mes.auto.exception.MultiGradeException;
import com.hengyi.japp.mes.auto.exception.SilkCarStatusException;
import com.hengyi.japp.mes.auto.interfaces.warehouse.WarehouseService;
import com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * 仓库接口
 *
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class WarehouseServiceImpl implements WarehouseService {
    private final PackageBoxRepository packageBoxRepository;
    private final PackageBoxFlipRepository packageBoxFlipRepository;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;
    private final SilkRepository silkRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private WarehouseServiceImpl(PackageBoxRepository packageBoxRepository, PackageBoxFlipRepository packageBoxFlipRepository, SilkCarRuntimeRepository silkCarRuntimeRepository, SilkRepository silkRepository, OperatorRepository operatorRepository) {
        this.packageBoxRepository = packageBoxRepository;
        this.packageBoxFlipRepository = packageBoxFlipRepository;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
        this.silkRepository = silkRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<PackageBox> handle(Principal principal, WarehousePackageBoxFetchEvent.Command command) {
        return packageBoxRepository.findByCode(command.getCode()).flatMap(packageBox -> {
            if (packageBox.getBudat() == null) {
                throw new RuntimeException("未计量");
            }
            packageBox.setInWarehouse(true);
            return operatorRepository.find(principal).flatMap(operator -> {
                packageBox.log(operator);
                return packageBoxRepository.save(packageBox);
            });
        });
    }

    @Override
    public Single<PackageBoxFlip> handle(Principal principal, PackageBoxFlipEvent.WarehouseCommand command) {
        PackageBoxFlipEvent event = new PackageBoxFlipEvent();
        event.setCommand(MAPPER.convertValue(this, JsonNode.class));
        return operatorRepository.find(principal).flatMap(operator -> {
            event.fire(operator);
            return packageBoxFlipRepository.create().flatMap(packageBoxFlip -> {
                packageBoxFlip.setType(PackageBoxFlipType.WAREHOUSE);
                packageBoxFlip.log(event.getOperator(), event.getFireDateTime());
                return packageBoxRepository.findByCode(command.getPackageBox().getCode()).flatMap(packageBox -> {
                    packageBoxFlip.setPackageBox(packageBox);
                    return Flowable.fromIterable(command.getInSilks())
                            .map(EntityByCodeDTO::getCode)
                            .flatMapMaybe(silkRepository::findByCode).toList();
                }).flatMap(inSilks -> {
                    packageBoxFlip.setInSilks(inSilks);
                    return getPairs(command);
                }).flatMap(pairs -> checkAndSave(event, packageBoxFlip, pairs));
            });
        });
    }

    private Single<List<Pair<SilkCarRuntime, List<SilkRuntime>>>> getPairs(PackageBoxFlipEvent.WarehouseCommand command) {
        final SilkCarRuntimeService silkCarRuntimeService = Jvertx.getProxy(SilkCarRuntimeService.class);

        return Flowable.fromIterable(command.getItems()).flatMapSingle(item -> silkCarRuntimeService.find(item.getSilkCarRecord()).map(silkCarRuntime -> {
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            final SilkCar silkCar = silkCarRecord.getSilkCar();
            final Set<String> silkIds = item.getSilks().stream().map(EntityDTO::getId).collect(Collectors.toSet());
            final List<SilkRuntime> silkRuntimes = silkCarRuntime.getSilkRuntimes().stream()
                    .filter(it -> silkIds.contains(it.getSilk().getId()))
                    .collect(Collectors.toList());
            if (silkIds.size() == silkRuntimes.size()) {
                return Pair.of(silkCarRuntime, silkRuntimes);
            }
            throw new SilkCarStatusException(silkCar);
        })).toList();
    }

    private Single<PackageBoxFlip> checkAndSave(PackageBoxFlipEvent event, PackageBoxFlip packageBoxFlip, List<Pair<SilkCarRuntime, List<SilkRuntime>>> pairs) throws MultiBatchException, MultiGradeException {
        final Set<Batch> batchSet = Sets.newConcurrentHashSet();
        final Set<Grade> gradeSet = Sets.newConcurrentHashSet();
        final Set<Completable> addEvents = Sets.newConcurrentHashSet();

        final PackageBox packageBox = packageBoxFlip.getPackageBox();
        batchSet.add(packageBox.getBatch());
        gradeSet.add(packageBox.getGrade());

        packageBoxFlip.getInSilks().forEach(silk -> {
            batchSet.add(silk.getBatch());
            gradeSet.add(silk.getGrade());
        });

        pairs.forEach(pair -> {
            final SilkCarRuntime silkCarRuntime = pair.getKey();
            final List<SilkRuntime> silkRuntimes = pair.getValue();
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            final SilkCar silkCar = silkCarRecord.getSilkCar();
            addEvents.add(silkCarRuntimeRepository.addEventSource(silkCar.getCode(), event));
            silkRuntimes.forEach(silkRuntime -> {
                final Silk silk = silkRuntime.getSilk();
                batchSet.add(silk.getBatch());
                gradeSet.add(silk.getGrade());
            });
        });

        if (batchSet.size() != 1) {
            throw new MultiBatchException();
        }
        if (gradeSet.size() != 1) {
            throw new MultiGradeException();
        }

        final Single<PackageBoxFlip> result$ = packageBoxFlipRepository.save(packageBoxFlip);
        return Completable.merge(addEvents).andThen(result$);
    }

}
