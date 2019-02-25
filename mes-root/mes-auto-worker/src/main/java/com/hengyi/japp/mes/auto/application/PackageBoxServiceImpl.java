package com.hengyi.japp.mes.auto.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.PackageBoxAppendCommand;
import com.hengyi.japp.mes.auto.application.command.PackageBoxBatchPrintUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.PackageBoxMeasureInfoUpdateCommand;
import com.hengyi.japp.mes.auto.application.event.PackageBoxEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.domain.data.TemporaryBoxRecordType;
import com.hengyi.japp.mes.auto.exception.MultiBatchException;
import com.hengyi.japp.mes.auto.exception.MultiGradeException;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class PackageBoxServiceImpl implements PackageBoxService {
    private final AuthService authService;
    private final PackageBoxRepository packageBoxRepository;
    private final OperatorRepository operatorRepository;
    private final PackageClassRepository packageClassRepository;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;
    private final SilkRepository silkRepository;
    private final SapT001lRepository sapT001lRepository;
    private final TemporaryBoxRepository temporaryBoxRepository;
    private final TemporaryBoxRecordRepository temporaryBoxRecordRepository;
    private final BatchRepository batchRepository;
    private final GradeRepository gradeRepository;

    @Inject
    private PackageBoxServiceImpl(AuthService authService, PackageBoxRepository packageBoxRepository, OperatorRepository operatorRepository, PackageClassRepository packageClassRepository, SilkCarRuntimeRepository silkCarRuntimeRepository, SilkRepository silkRepository, SapT001lRepository sapT001lRepository, TemporaryBoxRepository temporaryBoxRepository, TemporaryBoxRecordRepository temporaryBoxRecordRepository, BatchRepository batchRepository, GradeRepository gradeRepository) {
        this.authService = authService;
        this.packageBoxRepository = packageBoxRepository;
        this.operatorRepository = operatorRepository;
        this.packageClassRepository = packageClassRepository;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
        this.silkRepository = silkRepository;
        this.sapT001lRepository = sapT001lRepository;
        this.temporaryBoxRepository = temporaryBoxRepository;
        this.temporaryBoxRecordRepository = temporaryBoxRecordRepository;
        this.batchRepository = batchRepository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public Single<PackageBox> handle(Principal principal, PackageBoxEvent.ManualCommandSimple command) {
        final PackageBoxEvent event = new PackageBoxEvent();
        event.setCommand(MAPPER.convertValue(command, JsonNode.class));
        final Single<PackageBox> result$ = operatorRepository.find(principal).flatMap(operator -> {
            event.fire(operator);
            return packageBoxRepository.create();
        }).flatMap(packageBox -> {
            packageBox.log(event.getOperator(), event.getFireDateTime());
            packageBox.setPrintDate(event.getFireDateTime());
            packageBox.setSilkCount(command.getSilkCount());
            packageBox.setType(PackageBoxType.MANUAL);
            packageBox.command(event.getCommand());

            final SilkCarRuntimeService silkCarRuntimeService = Jvertx.getProxy(SilkCarRuntimeService.class);
            return Flowable.fromIterable(command.getSilkCarRecords())
                    .flatMapSingle(silkCarRuntimeService::find)
                    .toList().flatMap(silkCarRuntimes -> {
                        final Collection<SilkCarRecord> silkCarRecords = silkCarRuntimes.stream()
                                .map(SilkCarRuntime::getSilkCarRecord)
                                .collect(toSet());
                        final Batch batch = checkAndGetBatch(silkCarRuntimes);
                        final Grade grade = checkAndGetGrade(silkCarRuntimes);
                        packageBox.setSilkCarRecords(silkCarRecords);
                        packageBox.setBatch(batch);
                        packageBox.setGrade(grade);

                        return packageBoxRepository.save(packageBox)
                                .doOnSuccess(it -> {
                                    event.setPackageBox(it);
                                    Flowable.fromIterable(silkCarRecords)
                                            .flatMapCompletable(silkCarRecord -> silkCarRuntimeRepository.addEventSource(silkCarRecord, event))
                                            .subscribe();
                                });
                    });
        });
        final Completable auth$ = authService.checkRole(principal, RoleType.PACKAGE_BOX);
        return auth$.andThen(result$);
    }

    private Batch checkAndGetBatch(Collection<SilkCarRuntime> silkCarRuntimes) throws Exception {
        final Set<Batch> batchSet = Stream.concat(
                silkCarRuntimes.stream()
                        .map(SilkCarRuntime::getSilkRuntimes)
                        .flatMap(Collection::stream)
                        .map(SilkRuntime::getSilk)
                        .map(Silk::getBatch)
                        .distinct(),
                silkCarRuntimes.stream()
                        .map(SilkCarRuntime::getSilkCarRecord)
                        .map(SilkCarRecord::getBatch)
                        .distinct()
        ).collect(toSet());
        if (batchSet.size() == 1) {
            return IterableUtils.get(batchSet, 0);
        }
        throw new MultiBatchException();
    }

    private Grade checkAndGetGrade(Collection<SilkCarRuntime> silkCarRuntimes) throws Exception {
        final Set<Grade> gradeSet = silkCarRuntimes.stream()
                .map(SilkCarRuntime::getSilkRuntimes)
                .flatMap(Collection::stream)
                .map(SilkRuntime::getGrade)
                .collect(toSet());
        if (gradeSet.size() == 1) {
            return IterableUtils.get(gradeSet, 0);
        }
        throw new MultiGradeException();
    }

    @Override
    public Single<PackageBox> handle(Principal principal, PackageBoxEvent.ManualCommand command) {
        final PackageBoxEvent event = new PackageBoxEvent();
        event.setCommand(MAPPER.convertValue(command, JsonNode.class));
        final Single<PackageBox> result$ = operatorRepository.find(principal).flatMap(operator -> {
            event.fire(operator);
            return packageBoxRepository.create();
        }).flatMap(packageBox -> {
            packageBox.log(event.getOperator(), event.getFireDateTime());
            packageBox.setPrintDate(event.getFireDateTime());
            packageBox.setType(PackageBoxType.MANUAL);
            packageBox.command(event.getCommand());
            return command.checkAndGetData().flatMap(pairs -> {
                final List<SilkRuntime> silkRuntimes = pairs.stream().map(Pair::getRight).flatMap(Collection::stream).collect(Collectors.toList());
                final List<Silk> silks = silkRuntimes.stream().map(SilkRuntime::getSilk).collect(Collectors.toList());
                packageBox.setSilks(silks);
                packageBox.setSilkCount(silks.size());
                packageBox.setGrade(silkRuntimes.get(0).getGrade());
                packageBox.setBatch(silks.get(0).getBatch());
                return packageBoxRepository.save(packageBox).flatMap(it -> {
                    event.setPackageBox(it);
                    final List<Completable> completables = Stream.concat(
                            silks.stream().map(silk -> {
                                silk.setGrade(it.getGrade());
                                silk.setPackageBox(it);
                                silk.setPackageDateTime(event.getFireDateTime());
                                return silkRepository.save(silk).ignoreElement();
                            }),
                            pairs.stream().map(Pair::getLeft).map(SilkCarRuntime::getSilkCarRecord).map(silkCarRecord -> {
                                final SilkCar silkCar = silkCarRecord.getSilkCar();
                                return silkCarRuntimeRepository.addEventSource(silkCar.getCode(), event);
                            })
                    ).collect(Collectors.toList());
                    return Completable.merge(completables).andThen(Single.fromCallable(() -> it));
                });
            });
        });
        final Completable auth$ = authService.checkRole(principal, RoleType.PACKAGE_BOX);
        return auth$.andThen(result$);
    }

    @Override
    public Single<PackageBox> handle(Principal principal, PackageBoxEvent.TemporaryBoxCommand command) {
        final PackageBoxEvent event = new PackageBoxEvent();
        event.setCommand(MAPPER.convertValue(command, JsonNode.class));
        final Single<PackageBox> result$ = operatorRepository.find(principal).flatMap(operator -> {
            event.fire(operator);
            return packageBoxRepository.create();
        }).flatMap(packageBox -> {
            packageBox.log(event.getOperator(), event.getFireDateTime());
            packageBox.setType(PackageBoxType.MANUAL);
            packageBox.command(event.getCommand());
            return temporaryBoxRepository.find(command.getTemporaryBox().getId()).flatMap(temporaryBox -> {
                packageBox.setBatch(temporaryBox.getBatch());
                packageBox.setGrade(temporaryBox.getGrade());
                packageBox.setSilkCount(command.getCount());
                return packageBoxRepository.save(packageBox).flatMap(savedPackageBox -> temporaryBoxRecordRepository.create().flatMap(temporaryBoxRecord -> {
                    temporaryBoxRecord.setType(TemporaryBoxRecordType.PACKAGE_BOX);
                    temporaryBoxRecord.setTemporaryBox(temporaryBox);
                    temporaryBoxRecord.setPackageBox(savedPackageBox);
                    temporaryBoxRecord.setCount(packageBox.getSilkCount() * -1);
                    temporaryBoxRecord.log(event.getOperator(), event.getFireDateTime());
                    return temporaryBoxRecordRepository.save(temporaryBoxRecord).map(it -> savedPackageBox);
                }));
            });
        });
        final Completable auth$ = authService.checkRole(principal, RoleType.PACKAGE_BOX);
        return auth$.andThen(result$);
    }

    @Override
    public Single<PackageBox> update(Principal principal, String id, PackageBoxMeasureInfoUpdateCommand command) {
        return packageBoxRepository.find(id).flatMap(packageBox -> {
            if (PackageBoxType.AUTO != packageBox.getType()) {
                packageBox.setSilkCount(command.getSilkCount());
                packageBox.setNetWeight(command.getNetWeight());
                packageBox.setGrossWeight(command.getGrossWeight());
            }
            packageBox.setBudat(command.getBudat());
            packageBox.setPalletType(command.getPalletType());
            packageBox.setPackageType(command.getPackageType());
            packageBox.setSaleType(command.getSaleType());
            packageBox.setFoamType(command.getFoamType());
            packageBox.setFoamNum(command.getFoamNum());
            Optional.ofNullable(command.getPalletCode())
                    .filter(J::nonBlank)
                    .ifPresent(packageBox::setPalletCode);
            return packageClassRepository.find(command.getBudatClass().getId()).flatMap(it -> {
                packageBox.setBudatClass(it);
                return sapT001lRepository.find(command.getSapT001l().getId());
            }).flatMap(it -> {
                packageBox.setSapT001l(it);
                return operatorRepository.find(principal);
            }).flatMap(it -> {
                packageBox.log(it);
                return packageBoxRepository.save(packageBox);
            });
        });
    }

    @Override
    public Completable print(Principal principal, String id) {
        return operatorRepository.find(principal).flatMap(operator -> packageBoxRepository.find(id).flatMap(packageBox -> {
            final int printCount = packageBox.getPrintCount();
            packageBox.setPrintCount(printCount + 1);
            packageBox.log(operator);
            return packageBoxRepository.save(packageBox);
        })).ignoreElement();
    }

    @Override
    public Completable print(Principal principal, PackageBoxBatchPrintUpdateCommand command) {
        return Flowable.fromIterable(command.getPackageBoxes()).flatMapCompletable(it -> print(principal, it.getId()));
    }

    @Override
    public Completable delete(Principal principal, String id) {
        final Collection<PackageBoxType> canDeleteTypes = ImmutableSet.of(PackageBoxType.MANUAL_APPEND, PackageBoxType.MANUAL);
        return packageBoxRepository.find(id).flatMapCompletable(packageBox -> {
            if (!canDeleteTypes.contains(packageBox.getType())) {
                throw new RuntimeException();
            }
            packageBox.setDeleted(true);
            return operatorRepository.find(principal).flatMapCompletable(operator -> {
                packageBox.log(operator);
                final Completable saveSilks$ = Flowable.fromIterable(J.emptyIfNull(packageBox.getSilks())).flatMapCompletable(silk -> {
                    silk.setPackageBox(null);
                    silk.setPackageDateTime(null);
                    return silkRepository.save(silk).ignoreElement();
                });
                // todo 暂存箱打包
                final Completable savePackageBox$ = packageBoxRepository.save(packageBox).ignoreElement();
                return Completable.mergeArray(savePackageBox$, saveSilks$);
            });
        });
    }

    @Override
    public Single<PackageBox> handle(Principal principal, PackageBoxAppendCommand command) {
        final Completable check$ = authService.checkPermission(principal, "PackageBox:ManualAppend");
        final Single<PackageBox> result$ = packageBoxRepository.create().flatMap(packageBox -> {
            packageBox.setType(PackageBoxType.MANUAL_APPEND);
            packageBox.setSilkCount(command.getSilkCount());
            packageBox.setBudat(command.getBudat());
            packageBox.setPrintDate(command.getBudat());
            packageBox.setGrossWeight(command.getGrossWeight());
            packageBox.setNetWeight(command.getNetWeight());
            packageBox.setPalletType(command.getPalletType());
            packageBox.setPackageType(command.getPackageType());
            packageBox.setSaleType(command.getSaleType());
            packageBox.setFoamType(command.getFoamType());
            packageBox.setFoamNum(command.getFoamNum());
            return batchRepository.find(command.getBatch().getId()).flatMap(batch -> {
                packageBox.setBatch(batch);
                return gradeRepository.find(command.getGrade().getId());
            }).flatMap(grade -> {
                packageBox.setGrade(grade);
                return packageClassRepository.find(command.getBudatClass().getId());
            }).flatMap(budatClass -> {
                packageBox.setBudatClass(budatClass);
                packageBox.setPrintClass(budatClass);
                return sapT001lRepository.find(command.getSapT001l().getId());
            }).flatMap(it -> {
                packageBox.setSapT001l(it);
                return operatorRepository.find(principal);
            }).flatMap(operator -> {
                packageBox.log(operator, new Date());
                return packageBoxRepository.save(packageBox);
            });
        });
        return check$.andThen(result$);
    }

}
