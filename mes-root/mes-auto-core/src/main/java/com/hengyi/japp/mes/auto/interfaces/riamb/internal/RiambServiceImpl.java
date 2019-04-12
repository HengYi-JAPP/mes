package com.hengyi.japp.mes.auto.interfaces.riamb.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ApplicationEvents;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.SilkNoteFeedbackEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.domain.data.SaleType;
import com.hengyi.japp.mes.auto.interfaces.riamb.RiambService;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambFetchSilkCarRecordResultDTO;
import com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambPackageBoxEvent;
import com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkCarInfoFetchEvent;
import com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkDetachEvent;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;

import java.security.Principal;
import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambFetchSilkCarRecordResultDTO.*;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class RiambServiceImpl implements RiambService {
    private final ApplicationEvents applicationEvents;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;
    private final SilkRepository silkRepository;
    private final PackageBoxRepository packageBoxRepository;
    private final BatchRepository batchRepository;
    private final GradeRepository gradeRepository;
    private final PackageClassRepository packageClassRepository;
    private final SapT001lRepository sapT001lRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private RiambServiceImpl(ApplicationEvents applicationEvents, SilkCarRuntimeRepository silkCarRuntimeRepository, SilkRepository silkRepository, PackageBoxRepository packageBoxRepository, BatchRepository batchRepository, GradeRepository gradeRepository, PackageClassRepository packageClassRepository, SapT001lRepository sapT001lRepository, OperatorRepository operatorRepository) {
        this.applicationEvents = applicationEvents;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
        this.silkRepository = silkRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.batchRepository = batchRepository;
        this.gradeRepository = gradeRepository;
        this.packageClassRepository = packageClassRepository;
        this.sapT001lRepository = sapT001lRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<RiambFetchSilkCarRecordResultDTO> fetchSilkCarRecord(Principal principal, String code) {
        final Single<RiambSilkCarInfoFetchEvent> event$ = operatorRepository.find(principal).map(operator -> {
            final RiambSilkCarInfoFetchEvent event = new RiambSilkCarInfoFetchEvent();
            event.fire(operator);
            return event;
        });
        return event$.flatMap(event -> silkCarRuntimeRepository.findByCode(code).flatMapSingle(silkCarRuntime -> {
            final RiambFetchSilkCarRecordResultDTO dto = getResult(silkCarRuntime);
            event.setResult(MAPPER.writeValueAsString(dto));
            final Single<RiambFetchSilkCarRecordResultDTO> result$ = Single.fromCallable(() -> dto);
            if (packeFlage_NO.equals(dto.getPackeFlage())) {
                return result$;
            }
            final Completable addEventSource$ = silkCarRuntimeRepository.addEventSource(code, event);
            return addEventSource$.andThen(result$).doOnSuccess(it -> saveSilkExceptions(dto));
        }));
    }

    private RiambFetchSilkCarRecordResultDTO getResult(SilkCarRuntime silkCarRuntime) {
        final RiambFetchSilkCarRecordResultDTO dto = new RiambFetchSilkCarRecordResultDTO();
        final var silkCarInfo = new RiambFetchSilkCarRecordResultDTO.SilkCarInfo();
        final List<RiambFetchSilkCarRecordResultDTO.SilkInfo> silkInfos = Lists.newArrayList();
        dto.setSilkCarInfo(silkCarInfo);
        dto.setSilkInfos(silkInfos);

        final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
        final SilkCar silkCar = silkCarRecord.getSilkCar();
        final Batch batch = silkCarRecord.getBatch();
        final Collection<SilkRuntime> silkRuntimes = J.emptyIfNull(silkCarRuntime.getSilkRuntimes());

        silkCarInfo.setId(silkCarRecord.getId());
        silkCarInfo.setCode(silkCar.getCode());
        silkCarInfo.setRow(silkCar.getRow());
        silkCarInfo.setCol(silkCar.getCol());
        silkCarInfo.setBatchNo(batch.getBatchNo());

        //"Silk[" + silkRuntime.getSideType() + "-" + silkRuntime.getRow() + "-" + silkRuntime.getCol() + "]"
        final List<SilkRuntime> dyeingUnSubmitteds = Lists.newArrayList();
        final Set<SilkNote> feedbackSilkNotes = Sets.newHashSet();
        for (SilkRuntime silkRuntime : silkRuntimes) {
            final Silk silk = silkRuntime.getSilk();
            final LineMachine lineMachine = silk.getLineMachine();
            final Line line = lineMachine.getLine();
            final String spec = line.getName() + "-" + silk.getSpindle() + "/" + lineMachine.getItem();
            final Grade grade = Optional.ofNullable(silkRuntime.getGrade()).orElse(silkCarRecord.getGrade());

            final var silkInfo = new RiambFetchSilkCarRecordResultDTO.SilkInfo();
            silkInfos.add(silkInfo);
            feedbackSilkNotes.addAll(J.emptyIfNull(silkRuntime.getNotes()));

            silkInfo.setCode(silk.getCode());
            silkInfo.setSideType(silkRuntime.getSideType());
            silkInfo.setRow(silkRuntime.getRow());
            silkInfo.setCol(silkRuntime.getCol());
            silkInfo.setSpec(spec);
            silkInfo.setBatchNo(batch.getBatchNo());
            silkInfo.setGradeName(grade.getName());
            silkInfo.setDoffingNum(silk.getDoffingNum());
            silkInfo.setDoffingDateTime(silk.getDoffingDateTime());
            silkInfo.setDoffingOperatorName(silk.getDoffingOperator().getName());
            silkInfo.setDoffingType(silk.getDoffingType());

            final Collection<SilkException> silkExceptions = J.emptyIfNull(silkRuntime.getExceptions());
            silkInfo.setSilkExceptions(silkExceptions);
            silkInfo.setEliminateFlage(J.nonEmpty(silkExceptions) ? eliminateFlage_YES : eliminateFlage_NO);
            silkInfo.setDyeingSubmitted(true);
            if (silkCarRecord.getDoffingType() == null) {
                final SilkRuntime.DyeingResultInfo multiDyeingResultInfo = silkRuntime.getMultiDyeingResultInfo();
                silkInfo.accept(multiDyeingResultInfo);
            } else {
                final SilkRuntime.DyeingResultInfo firstDyeingResultInfo = silkRuntime.getFirstDyeingResultInfo();
                silkInfo.accept(firstDyeingResultInfo);
                final SilkRuntime.DyeingResultInfo crossDyeingResultInfo = silkRuntime.getCrossDyeingResultInfo();
                silkInfo.accept(crossDyeingResultInfo);
            }
            if (J.nonEmpty(silkInfo.getDyeingExceptionStrings())) {
                silkInfo.setEliminateFlage(eliminateFlage_YES);
            }
            if (grade.getSortBy() < 100) {
                silkInfo.setEliminateFlage(eliminateFlage_YES);
            }
            if (silkInfo.isDyeingSubmitted()) {
                silkInfo.setGrabFlage(grabFlage_YES);
            } else {
                silkInfo.setGrabFlage(grabFlage_NO);
                dyeingUnSubmitteds.add(silkRuntime);
            }
        }

        dto.setPackeFlage(J.isEmpty(dyeingUnSubmitteds) ? packeFlage_YES : packeFlage_NO);
        J.emptyIfNull(silkCarRuntime.getEventSources()).stream()
                .filter(it -> !it.isDeleted() && it.getType() == EventSourceType.SilkNoteFeedbackEvent)
                .forEach(it -> {
                    final SilkNoteFeedbackEvent silkNoteFeedbackEvent = (SilkNoteFeedbackEvent) it;
                    final SilkNote silkNote = silkNoteFeedbackEvent.getSilkNote();
                    feedbackSilkNotes.remove(silkNote);
                });
        final Set<SilkNote> checkFeedbackSilkNotes = J.emptyIfNull(feedbackSilkNotes).stream()
                .filter(SilkNote::isMustFeedback)
                .collect(toSet());
        if (J.nonEmpty(checkFeedbackSilkNotes)) {
            dto.setPackeFlage(packeFlage_NO);
        }
        if (packeFlage_NO.equals(dto.getPackeFlage())) {
            final List<String> reasons = Lists.newArrayList();
            if (J.nonEmpty(dyeingUnSubmitteds)) {
                reasons.add("染判结果未出");
            }
            if (J.nonEmpty(checkFeedbackSilkNotes)) {
                checkFeedbackSilkNotes.forEach(silkNote -> {
                    final String name = silkNote.getName();
                    reasons.add(name + "未处理");
                });
            }
            applicationEvents.fire(silkCarRuntime, dto, reasons);
        }
        dto.setSilkCount(silkInfos.size());
        return dto;
    }

    private void saveSilkExceptions(RiambFetchSilkCarRecordResultDTO dto) {
        Flowable.fromIterable(J.emptyIfNull(dto.getSilkInfos())).flatMapCompletable(silkInfo -> {
            final String code = silkInfo.getCode();
            return silkRepository.findByCode(code).flatMapSingle(silk -> {
                final Set<SilkException> silkExceptions = Sets.newHashSet(J.emptyIfNull(silk.getExceptions()));
                silkExceptions.addAll(J.emptyIfNull(silkInfo.getSilkExceptions()));
                silk.setExceptions(silkExceptions);
                final Set<String> dyeingExceptionStrings = Sets.newHashSet(J.emptyIfNull(silk.getDyeingExceptionStrings()));
                dyeingExceptionStrings.addAll(J.emptyIfNull(silkInfo.getDyeingExceptionStrings()));
                silk.setDyeingExceptionStrings(dyeingExceptionStrings);
                return silkRepository.save(silk);
            }).ignoreElement();
        }).doOnError(ex -> log.error("saveSilkExceptions", ex)).subscribe();
    }

    @Override
    public Completable handle(Principal principal, RiambSilkDetachEvent.Command command) {
        final var event$ = operatorRepository.find(principal).map(operator -> {
            final RiambSilkDetachEvent event = new RiambSilkDetachEvent();
            event.fire(operator);
            event.setCommand(command);
            return event;
        });
        return event$.flatMapCompletable(event -> {
            final var silkCarInfo = command.getSilkCarInfo();
            return silkCarRuntimeRepository.addEventSource(silkCarInfo.getCode(), event);
        });
    }

    @Override
    public Completable packageBox(Principal principal, RiambPackageBoxEvent.Command command) {
        return packageBoxRepository.findOrCreateByCode(command.getCode()).flatMapCompletable(packageBox -> {
            final var jobInfo = command.getJobInfo();

            packageBox.setType(PackageBoxType.AUTO);
            packageBox.command(MAPPER.convertValue(command, JsonNode.class));
            packageBox.setCode(command.getCode());
            packageBox.setNetWeight(command.getNetWeight().doubleValue());
            packageBox.setGrossWeight(command.getGrossWeight().doubleValue());
            packageBox.setSilkCount(command.getSilkCount());
            packageBox.setPrintDate(command.getCreateDateTime());
            packageBox.setCreateDateTime(command.getCreateDateTime());
            packageBox.setPalletCode(command.getPalletCode());

            packageBox.setRiambJobId(jobInfo.getId());
            packageBox.setAutomaticPackeLine(jobInfo.getAutomaticPackeLine());
            packageBox.setBudat(jobInfo.getBudatDate());
            packageBox.setPackageType(jobInfo.getPackageType());
            packageBox.setPalletType(jobInfo.getPalletType());
            packageBox.setFoamType(jobInfo.getFoamType());
            packageBox.setFoamNum(jobInfo.getFoamNum());
            packageBox.setSaleType(getSaleType(jobInfo));

            return packageClassRepository.findByName(jobInfo.getPackageClassNo()).flatMap(packageClass -> {
                packageBox.setPrintClass(packageClass);
                packageBox.setBudatClass(packageClass);
                return gradeRepository.findByName(jobInfo.getGradeName());
            }).flatMap(grade -> {
                packageBox.setGrade(grade);
                return batchRepository.findByBatchNo(jobInfo.getBatchNo());
            }).flatMap(batch -> {
                packageBox.setBatch(batch);
                return operatorRepository.findByHrId(jobInfo.getCreatorHrId());
            }).flatMapSingle(operator -> {
                packageBox.log(operator, command.getCreateDateTime());
                return sapT001lRepository.find(jobInfo.getLgort());
            }).flatMap(sapT001l -> {
                packageBox.setSapT001l(sapT001l);
                return Flowable.fromIterable(command.getSilkInfos())
                        .map(RiambPackageBoxEvent.SilkInfo::getCode)
                        .flatMapMaybe(silkRepository::findByCode)
                        .toList();
            }).flatMapCompletable(silks -> {
                packageBox.setSilks(silks);
                if (packageBox.getSilkCount() != silks.size()) {
                    log.error("PackageBox[" + packageBox.getCode() + "],丝锭颗数不符,实际[" + packageBox.getSilkCount() + "],落丝[" + silks.size() + "]!");
                }
                final Set<Batch> batches = J.emptyIfNull(silks).stream().map(Silk::getBatch).collect(toSet());
                if (batches.size() == 1) {
                    final Batch batch = IterableUtils.get(batches, 0);
                    if (!Objects.equals(packageBox.getBatch(), batch)) {
                        log.error("PackageBox[" + packageBox.getCode() + "],混批!");
                    }
                } else {
                    log.error("PackageBox[" + packageBox.getCode() + "],混批!");
                }
                return packageBoxRepository.save(packageBox).flatMapCompletable(it -> Flowable.fromIterable(silks).flatMapCompletable(silk -> {
                    silk.setPackageBox(it);
                    silk.setPackageDateTime(it.getPrintDate());
                    silk.setGrade(it.getGrade());
                    return silkRepository.save(silk).ignoreElement();
                }));
            });
        });
    }

    private SaleType getSaleType(RiambPackageBoxEvent.AutomaticPackeJobInfo jobInfo) {
        switch (jobInfo.getSaleType()) {
            case "FOREIGN":
            case "外贸":
                return SaleType.FOREIGN;

            case "DOMESTIC":
            case "内销":
            case "内贸":
                return SaleType.DOMESTIC;
        }
        return null;
    }

}
