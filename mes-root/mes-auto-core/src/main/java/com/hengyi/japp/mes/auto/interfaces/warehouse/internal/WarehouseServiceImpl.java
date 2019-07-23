package com.hengyi.japp.mes.auto.interfaces.warehouse.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkCarRuntimeService;
import com.hengyi.japp.mes.auto.application.event.PackageBoxEvent;
import com.hengyi.japp.mes.auto.application.event.PackageBoxFlipEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxFlipType;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.dto.EntityByCodeDTO;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.exception.MultiBatchException;
import com.hengyi.japp.mes.auto.exception.MultiGradeException;
import com.hengyi.japp.mes.auto.exception.SilkCarRuntimePackagedException;
import com.hengyi.japp.mes.auto.exception.SilkCarStatusException;
import com.hengyi.japp.mes.auto.interfaces.warehouse.WarehouseService;
import com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toSet;

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
    private final SapT001lRepository sapT001lRepository;
    private final PackageClassRepository packageClassRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private WarehouseServiceImpl(PackageBoxRepository packageBoxRepository, PackageBoxFlipRepository packageBoxFlipRepository, SilkCarRuntimeRepository silkCarRuntimeRepository, SilkRepository silkRepository, SapT001lRepository sapT001lRepository, PackageClassRepository packageClassRepository, OperatorRepository operatorRepository) {
        this.packageBoxRepository = packageBoxRepository;
        this.packageBoxFlipRepository = packageBoxFlipRepository;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
        this.silkRepository = silkRepository;
        this.sapT001lRepository = sapT001lRepository;
        this.packageClassRepository = packageClassRepository;
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
    public Completable unFetch(Principal principal, String code) {
        return packageBoxRepository.findByCode(code).flatMap(packageBox -> {
            if (!packageBox.isInWarehouse()) {
                throw new RuntimeException("未入库");
            }
            packageBox.setInWarehouse(false);
            return operatorRepository.find(principal).flatMap(operator -> {
                packageBox.log(operator);
                return packageBoxRepository.save(packageBox);
            });
        }).ignoreElement();
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
            final Set<String> silkIds = item.getSilks().stream().map(EntityDTO::getId).collect(toSet());
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

    @Override
    public Single<PackageBox> handle(Principal principal, PackageBoxEvent.BigSilkCarCommand command) {
        final SilkCarRuntimeService silkCarRuntimeService = Jvertx.getProxy(SilkCarRuntimeService.class);
        final PackageBoxEvent event = new PackageBoxEvent();
        event.setCommand(MAPPER.convertValue(command, JsonNode.class));
        return operatorRepository.find(principal).flatMap(operator -> {
            event.fire(operator);
            return Flowable.fromIterable(command.getSilkCarRecords()).flatMapSingle(silkCarRuntimeService::find).toList();
        }).flatMap(silkCarRuntimes -> {
            final Set<SilkCarRuntime> packagedSilkCarRuntimes = silkCarRuntimes.stream().filter(SilkCarRuntime::hasPackageBoxEvent).collect(toSet());
            if (J.nonEmpty(packagedSilkCarRuntimes)) {
                throw new SilkCarRuntimePackagedException(packagedSilkCarRuntimes);
            }

            final Set<SilkCarRecord> silkCarRecords = silkCarRuntimes.stream().map(SilkCarRuntime::getSilkCarRecord).collect(toSet());
            final Set<Silk> silks = silkCarRuntimes.stream().map(SilkCarRuntime::getSilkRuntimes).flatMap(Collection::stream).map(SilkRuntime::getSilk).collect(toSet());
            final Set<Batch> checkBatches = Stream.concat(
                    silkCarRecords.stream().map(SilkCarRecord::getBatch),
                    silks.stream().map(Silk::getBatch)
            ).collect(toSet());
            if (checkBatches.size() != 1) {
                throw new MultiBatchException();
            }
            final Batch batch = IterableUtils.get(checkBatches, 0);

            final Set<Grade> checkGrades = silkCarRecords.stream().map(SilkCarRecord::getGrade).collect(toSet());
            if (checkGrades.size() != 1) {
                throw new MultiGradeException();
            }
            final Grade grade = IterableUtils.get(checkGrades, 0);

            return packageBoxRepository.create().flatMap(packageBox -> {
                packageBox.setType(PackageBoxType.BIG_SILK_CAR);
                packageBox.log(event.getOperator(), event.getFireDateTime());
                packageBox.setPrintDate(event.getFireDateTime());
                packageBox.command(event.getCommand());
                packageBox.setSilkCarRecords(silkCarRecords);
                packageBox.setSilks(silks);
                packageBox.setSilkCount(silks.size());
                packageBox.setBatch(batch);
                packageBox.setGrade(grade);
                packageBox.setSaleType(command.getSaleType());
                packageBox.setBudat(command.getBudat());
                packageBox.setNetWeight(command.getNetWeight());
                packageBox.setGrossWeight(command.getGrossWeight());
                packageBox.setPipeType(command.getPipeType());
                return sapT001lRepository.find(command.getSapT001l().getId()).flatMap(sapT001l -> {
                    packageBox.setSapT001l(sapT001l);
                    return packageClassRepository.find(command.getBudatClass().getId());
                }).flatMap(budatClass -> {
                    packageBox.setBudatClass(budatClass);
                    return packageBoxRepository.save(packageBox);
                }).doOnSuccess(it -> {
                    event.setPackageBox(it);
                    Flowable.fromIterable(silkCarRecords)
                            .flatMapCompletable(silkCarRecord -> silkCarRuntimeRepository.addEventSource(silkCarRecord, event))
                            .subscribe();
                });
            });
        });
    }

}
