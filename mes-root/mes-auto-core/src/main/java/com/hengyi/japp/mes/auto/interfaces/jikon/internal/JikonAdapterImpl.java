package com.hengyi.japp.mes.auto.interfaces.jikon.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ApplicationEvents;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent;
import com.hengyi.japp.mes.auto.application.event.SilkNoteFeedbackEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.interfaces.facevisa.FacevisaService;
import com.hengyi.japp.mes.auto.interfaces.jikon.JikonAdapter;
import com.hengyi.japp.mes.auto.interfaces.jikon.JikonUtil;
import com.hengyi.japp.mes.auto.interfaces.jikon.dto.GetSilkSpindleInfoDTO;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkCarInfoFetchEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;

import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.interfaces.jikon.dto.GetSilkSpindleInfoDTO.*;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2018-06-20
 */
@Slf4j
@Singleton
public class JikonAdapterImpl implements JikonAdapter {
    private final ApplicationEvents applicationEvents;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;
    private final SilkRepository silkRepository;
    private final PackageBoxRepository packageBoxRepository;
    private final GradeRepository gradeRepository;
    private final PackageClassRepository packageClassRepository;
    private final OperatorRepository operatorRepository;
    private final ProductProcessRepository productProcessRepository;

    @Inject
    private JikonAdapterImpl(ApplicationEvents applicationEvents, SilkCarRuntimeRepository silkCarRuntimeRepository, SilkRepository silkRepository, PackageBoxRepository packageBoxRepository, GradeRepository gradeRepository, PackageClassRepository packageClassRepository, OperatorRepository operatorRepository, ProductProcessRepository productProcessRepository) {
        this.applicationEvents = applicationEvents;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
        this.silkRepository = silkRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.gradeRepository = gradeRepository;
        this.packageClassRepository = packageClassRepository;
        this.operatorRepository = operatorRepository;
        this.productProcessRepository = productProcessRepository;
    }

    @Override
    public Single<String> handle(Principal principal, JikonAdapterSilkCarInfoFetchEvent.Command command) {
        final String silkcarCode = command.getSilkcarCode();
        return command.toEvent(principal).flatMap(event -> silkCarRuntimeRepository.findByCode(silkcarCode).flatMapSingle(silkCarRuntime -> {
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            final Batch batch = silkCarRecord.getBatch();
            final Product product = batch.getProduct();
            return productProcessRepository.listBy(product).toList().flatMap(productProcesses -> {
                final GetSilkSpindleInfoDTO dto = getResult(silkCarRuntime, productProcesses);
                event.setResult(JikonUtil.success(dto));
                final Single<String> result$ = Single.fromCallable(() -> event.getResult());

                if (eliminateFlage_NO.equals(dto.getAutomaticPackeFlage())) {
                    return result$;
                }
                final Completable addEventSource$ = silkCarRuntimeRepository.addEventSource(silkcarCode, event);
                return addEventSource$.andThen(result$)
                        .doOnSuccess(it -> saveSilkExceptions(dto))
                        .doOnSuccess(it -> prepareFacevisa(dto));
            });
        })).onErrorReturn(JikonUtil::error);
    }

    private void saveSilkExceptions(GetSilkSpindleInfoDTO dto) {
        Flowable.fromIterable(J.emptyIfNull(dto.getList())).flatMapCompletable(item -> {
            final String code = item.getSpindleCode();
            return silkRepository.findByCode(code).flatMapSingle(silk -> {
                final HashSet<SilkException> silkExceptions = Sets.newHashSet(J.emptyIfNull(silk.getExceptions()));
                silkExceptions.addAll(J.emptyIfNull(item.getSilkExceptions()));
                silk.setExceptions(silkExceptions);
                final HashSet<String> dyeingExceptionStrings = Sets.newHashSet(J.emptyIfNull(silk.getDyeingExceptionStrings()));
                dyeingExceptionStrings.addAll(J.emptyIfNull(item.getDyeingExceptionStrings()));
                silk.setDyeingExceptionStrings(dyeingExceptionStrings);
                return silkRepository.save(silk);
            }).ignoreElement();
        }).doOnError(ex -> log.error("saveSilkExceptions", ex)).subscribe();
    }

    private void prepareFacevisa(GetSilkSpindleInfoDTO dto) {
        if (eliminateFlage_NO.equals(dto.getAutomaticPackeFlage())) {
            return;
        }
        final FacevisaService facevisaService = Jvertx.getProxy(FacevisaService.class);
        facevisaService.prepare(dto).subscribe();
    }

    private GetSilkSpindleInfoDTO getResult(SilkCarRuntime silkCarRuntime, List<ProductProcess> allProductProcesses) {
        final GetSilkSpindleInfoDTO dto = new GetSilkSpindleInfoDTO();
        final Collection<SilkRuntime> silkRuntimes = J.emptyIfNull(silkCarRuntime.getSilkRuntimes());
        final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
        final Collection<EventSource> eventSources = silkCarRuntime.getEventSources();
        final SilkCar silkCar = silkCarRecord.getSilkCar();
        final Batch batch = silkCarRecord.getBatch();
        final int spec = silkCar.getRow() * silkCar.getCol() * 2;
        dto.setSpec("" + spec);

        final Set<ProductProcess> unProductProcesses = J.emptyIfNull(allProductProcesses).stream().filter(ProductProcess::isMustProcess).collect(toSet());
        J.emptyIfNull(eventSources).stream()
                .filter(it -> !it.isDeleted() && it.getType() == EventSourceType.ProductProcessSubmitEvent)
                .forEach(eventSource -> {
                    final ProductProcessSubmitEvent productProcessSubmitEvent = (ProductProcessSubmitEvent) eventSource;
                    final ProductProcess productProcess = productProcessSubmitEvent.getProductProcess();
                    unProductProcesses.remove(productProcess);
                });

        final List<GetSilkSpindleInfoDTO.Item> items = Lists.newArrayList();
        //"Silk[" + silkRuntime.getSideType() + "-" + silkRuntime.getRow() + "-" + silkRuntime.getCol() + "]"
        final List<SilkRuntime> dyeingUnSubmitteds = Lists.newArrayList();
        final Set<SilkNote> feedbackSilkNotes = Sets.newHashSet();
        for (SilkRuntime silkRuntime : silkRuntimes) {
            final Silk silk = silkRuntime.getSilk();
            final Grade grade = Optional.ofNullable(silkRuntime.getGrade()).orElse(silkCarRecord.getGrade());
            final Collection<SilkException> silkExceptions = J.emptyIfNull(silkRuntime.getExceptions());

            final GetSilkSpindleInfoDTO.Item item = new GetSilkSpindleInfoDTO.Item();
            items.add(item);
            feedbackSilkNotes.addAll(J.emptyIfNull(silkRuntime.getNotes()));

            item.setSilkRuntime(silkRuntime);
            item.setSpindleCode(silk.getCode());
            item.setBatchNo(batch.getBatchNo());
            item.setGrade(grade.getId());
            item.setActualPosition(calcActualPosition(silkCar, silkRuntime));
            item.setSilkExceptions(silkExceptions);
            item.setEliminateFlage(J.nonEmpty(silkExceptions) ? eliminateFlage_YES : eliminateFlage_NO);
            item.setDyeingSubmitted(true);
            if (silkCarRecord.getDoffingType() == null) {
                final SilkRuntime.DyeingResultInfo multiDyeingResultInfo = silkRuntime.getMultiDyeingResultInfo();
                item.accept(multiDyeingResultInfo);
            } else {
                final SilkRuntime.DyeingResultInfo selfDyeingResultInfo = silkRuntime.getSelfDyeingResultInfo();
                if (selfDyeingResultInfo != null) {
                    item.accept(selfDyeingResultInfo);
                } else {
                    final SilkRuntime.DyeingResultInfo firstDyeingResultInfo = silkRuntime.getFirstDyeingResultInfo();
                    item.accept(firstDyeingResultInfo);
                    final SilkRuntime.DyeingResultInfo crossDyeingResultInfo = silkRuntime.getCrossDyeingResultInfo();
                    item.accept(crossDyeingResultInfo);
                }
            }
            if (J.nonEmpty(item.getDyeingExceptionStrings())) {
                item.setEliminateFlage(eliminateFlage_YES);
            }
            if (item.isDyeingSubmitted()) {
                item.setGrabFlage(grabFlage_YES);
            } else {
                item.setGrabFlage(grabFlage_NO);
                dyeingUnSubmitteds.add(silkRuntime);
            }
        }

        dto.setAutomaticPackeFlage(J.isEmpty(dyeingUnSubmitteds) ? AutomaticPackeFlage_YES : AutomaticPackeFlage_NO);
        J.emptyIfNull(eventSources).stream()
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
            dto.setAutomaticPackeFlage(AutomaticPackeFlage_NO);
        }
        if (silkCarRecord.getCarpoolDateTime() == null) {
            if (J.nonEmpty(unProductProcesses)) {
                dto.setAutomaticPackeFlage(AutomaticPackeFlage_NO);
            }
        }

        if (AutomaticPackeFlage_NO.equals(dto.getAutomaticPackeFlage())) {
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
            if (silkCarRecord.getCarpoolDateTime() == null) {
                if (J.nonEmpty(unProductProcesses)) {
                    unProductProcesses.forEach(productProcess -> {
                        final String name = productProcess.getName();
                        reasons.add(name + "未处理");
                    });
                }
            }
            applicationEvents.fire(silkCarRuntime, dto, reasons);
        }

        dto.setBindNum("" + items.size());
        dto.setList(items);
        return dto;
    }

    private String calcActualPosition(SilkCar silkCar, SilkRuntime silkRuntime) {
        final int silkCarRow = silkCar.getRow();
        final int silkCarCol = silkCar.getCol();
        final int oneSideCount = silkCarRow * silkCarCol;
        final int row = silkRuntime.getRow();
        final int col = silkRuntime.getCol();
        final int sidePosition = (row - 1) * silkCarCol + col;
        final int result = silkRuntime.getSideType() == SilkCarSideType.A ? sidePosition : (oneSideCount + sidePosition);
        return "" + result;
    }

    @Override
    public Single<String> handle(Principal principal, JikonAdapterSilkDetachEvent.Command command) {
        return command.toEvent(principal).flatMap(event -> {
            final String silkcarCode = command.getSilkcarCode();
            if (J.isBlank(silkcarCode)) {
                return Single.just(JikonUtil.ok());
            }
            return silkCarRuntimeRepository.addEventSource(silkcarCode, event)
                    .andThen(Single.just(JikonUtil.ok()));
        });
    }

    @Override
    public Single<String> handle(Principal principal, JikonAdapterPackageBoxEvent.Command command) {
        final Single<Map<String, PackageClass>> packageClassMap$ = packageClassRepository.list().toMap(PackageClass::getRiambCode);
        final Single<Map<String, Grade>> gradeMap$ = gradeRepository.list().toMap(Grade::getName);
        @NotBlank final String boxCode = command.getBoxCode();
        return packageClassMap$.flatMap(packageClassMap -> gradeMap$.flatMap(gradeMap -> packageBoxRepository.findByCodeOrCreate(boxCode).flatMap(packageBox -> {
            packageBox.setType(PackageBoxType.AUTO);
            packageBox.command(MAPPER.convertValue(command, JsonNode.class));
            packageBox.setCode(boxCode);
            packageBox.setNetWeight(Double.parseDouble(command.getNetWeight()));
            packageBox.setGrossWeight(Double.parseDouble(command.getGrossWeight()));
            packageBox.setAutomaticPackeLine(command.getAutomaticPackeLine());
            packageBox.setPrintDate(new Date());
            packageBox.setPalletCode(command.getPalletCode());
            final PackageClass printClass = packageClassMap.get(command.getClassno());
            packageBox.setPrintClass(printClass);
            final Grade grade = gradeMap.get(command.getGrade());
            packageBox.setGrade(grade);
            return Flowable.fromIterable(command.getSpindle()).flatMapSingle(item ->
                    silkRepository.findByCode(item.getSpindleCode()).toSingle()
            ).toList().flatMap(silks -> {
                packageBox.setSilks(silks);
                packageBox.setSilkCount(silks.size());
                final Set<Batch> batches = J.emptyIfNull(silks).stream().map(Silk::getBatch).collect(toSet());
                if (batches.size() == 1) {
                    final Batch batch = IterableUtils.get(batches, 0);
                    packageBox.setBatch(batch);
                } else {
                    log.error("PackageBox[" + packageBox.getCode() + "],混批!");
//                    packageBox.setBatch(IterableUtils.get(batches, 0));
                }
                return operatorRepository.find(principal).flatMap(operator -> {
                    packageBox.log(operator);
                    return packageBoxRepository.save(packageBox);
                }).flatMap(it -> Flowable.fromIterable(J.emptyIfNull(silks))
                        .flatMapCompletable(silk -> {
                            silk.setGrade(packageBox.getGrade());
                            silk.setPackageBox(it);
                            silk.setPackageDateTime(packageBox.getPrintDate());
                            return silkRepository.save(silk).ignoreElement();
                        }).toSingleDefault(it)
                );
            });
        }))).map(packageBox -> JikonUtil.ok()).onErrorReturn(ex -> JikonUtil.error(ex));
    }

}
