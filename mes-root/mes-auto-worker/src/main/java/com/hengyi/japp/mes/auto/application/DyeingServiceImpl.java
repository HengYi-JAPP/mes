package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.DyeingResultUpdateCommand;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.UnicastSubject;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzb 2018-08-08
 */
@Slf4j
@Singleton
public class DyeingServiceImpl implements DyeingService {
    private final RedisClient redisClient;
    private final DyeingPrepareRepository dyeingPrepareRepository;
    private final DyeingResultRepository dyeingResultRepository;
    private final OperatorRepository operatorRepository;
    private final GradeRepository gradeRepository;
    private final SilkExceptionRepository silkExceptionRepository;
    private final SilkNoteRepository silkNoteRepository;
    private final LineMachineRepository lineMachineRepository;
    private static final Executor exe = Executors.newSingleThreadExecutor();
    private final UnicastSubject<DyeingResult> dyeingTimeLineUpdateSubject = UnicastSubject.create();

    @Inject
    private DyeingServiceImpl(RedisClient redisClient, DyeingPrepareRepository dyeingPrepareRepository, DyeingResultRepository dyeingResultRepository, OperatorRepository operatorRepository, GradeRepository gradeRepository, SilkExceptionRepository silkExceptionRepository, SilkNoteRepository silkNoteRepository, LineMachineRepository lineMachineRepository) {
        this.redisClient = redisClient;
        this.dyeingPrepareRepository = dyeingPrepareRepository;
        this.dyeingResultRepository = dyeingResultRepository;
        this.operatorRepository = operatorRepository;
        this.gradeRepository = gradeRepository;
        this.silkExceptionRepository = silkExceptionRepository;
        this.silkNoteRepository = silkNoteRepository;
        this.lineMachineRepository = lineMachineRepository;
        dyeingTimeLineUpdateSubject.subscribeOn(Schedulers.from(exe)).subscribe(this::updateTimeLine);
    }

    @Override
    public Single<DyeingPrepare> create(DyeingPrepare dyeingPrepare, SilkCarRuntime silkCarRuntime, Collection<SilkRuntime> silkRuntimes) {
        final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
        final Collection<Silk> silks = silkRuntimes.stream().map(SilkRuntime::getSilk).collect(Collectors.toList());
        dyeingPrepare.setSilkCarRecord(silkCarRecord);
        dyeingPrepare.setSilks(silks);
        return createDyeingResults(dyeingPrepare, silkRuntimes).flatMap(dyeingResults -> {
            dyeingPrepare.setDyeingResults(dyeingResults);
            return dyeingPrepareRepository.save(dyeingPrepare).doOnSuccess(this::updateTimeLine);
        });
    }

    private void updateTimeLine(DyeingPrepare dyeingPrepare) {
        Optional.ofNullable(dyeingPrepare)
                .map(DyeingPrepare::getDyeingResults)
                .orElse(Collections.emptyList())
                .forEach(dyeingTimeLineUpdateSubject::onNext);
    }

    private void updateTimeLine(DyeingResult dyeingResult) {
        final LineMachine lineMachine = dyeingResult.getLineMachine();
        final int spindle = dyeingResult.getSpindle();
        final DyeingPrepare dyeingPrepare = dyeingResult.getDyeingPrepare();
        final String key;
        switch (dyeingPrepare.getType()) {
            case FIRST:
                key = DyeingService.firstDyeingKey(lineMachine, spindle);
                break;
            case CROSS_LINEMACHINE_LINEMACHINE:
            case CROSS_LINEMACHINE_SPINDLE:
                key = DyeingService.crossDyeingKey(lineMachine, spindle);
                break;
            default:
                return;
        }

        redisClient.rxHgetall(key).flatMapCompletable(it -> {
            if (it.isEmpty()) {
                final JsonObject redisJson = dyeingResult.toRedisJsonObject();
                return redisClient.rxHmset(key, redisJson).ignoreElement();
            }
            return DyeingResult.fromRedis(it).flatMapCompletable(latest -> {
                final Completable timeLine$ = Flowable.fromIterable(latest.linkPush(dyeingResult))
                        .flatMapSingle(dyeingResultRepository::save)
                        .toList().ignoreElement();
                final Completable updateRedis$;
                if (dyeingResult.getDateTime().getTime() >= latest.getDateTime().getTime()) {
                    updateRedis$ = redisClient.rxHmset(key, dyeingResult.toRedisJsonObject()).ignoreElement();
                } else {
                    updateRedis$ = Completable.complete();
                }
                return Completable.mergeArray(timeLine$, updateRedis$);
            });
        }).subscribe();
    }

    private Single<List<DyeingResult>> createDyeingResults(DyeingPrepare dyeingPrepare, Collection<SilkRuntime> silkRuntimes) {
        return Flowable.fromIterable(silkRuntimes).flatMapSingle(silkRuntime -> {
            final Silk silk = silkRuntime.getSilk();
            final LineMachine lineMachine = silk.getLineMachine();
            final int spindle = silk.getSpindle();
            final Date dateTime = silk.getDoffingDateTime();
            return dyeingResultRepository.create().flatMap(dyeingResult -> {
                dyeingResult.setDyeingPrepare(dyeingPrepare);
                dyeingResult.setSilk(silk);
                dyeingResult.setLineMachine(lineMachine);
                dyeingResult.setSpindle(spindle);
                dyeingResult.setDateTime(dateTime);
                return dyeingResultRepository.save(dyeingResult);
            });
        }).toList();
    }

    @Override
    public Single<DyeingPrepare> create(DyeingPrepare dyeingPrepare, SilkCarRuntime silkCarRuntime1, Collection<SilkRuntime> silkRuntimes1, SilkCarRuntime silkCarRuntime2, Collection<SilkRuntime> silkRuntimes2) {
        final SilkCarRecord silkCarRecord1 = silkCarRuntime1.getSilkCarRecord();
        final Collection<Silk> silks1 = silkRuntimes1.stream()
                .map(SilkRuntime::getSilk)
                .collect(Collectors.toList());
        final SilkCarRecord silkCarRecord2 = silkCarRuntime2.getSilkCarRecord();
        final Collection<Silk> silks2 = silkRuntimes2.stream()
                .map(SilkRuntime::getSilk)
                .collect(Collectors.toList());
        dyeingPrepare.setSilkCarRecord1(silkCarRecord1);
        dyeingPrepare.setSilks1(silks1);
        dyeingPrepare.setSilkCarRecord2(silkCarRecord2);
        dyeingPrepare.setSilks2(silks2);

        final List<SilkRuntime> silkRuntimes = Stream.concat(silkRuntimes1.stream(), silkRuntimes2.stream()).collect(Collectors.toList());
        return createDyeingResults(dyeingPrepare, silkRuntimes).flatMap(dyeingResults -> {
            dyeingPrepare.setDyeingResults(dyeingResults);
            return dyeingPrepareRepository.save(dyeingPrepare).doOnSuccess(this::updateTimeLine);
        });
    }

    private Maybe<DyeingResult> rxLatest(String key) {
        return redisClient.rxHgetall(key).filter(it -> !it.isEmpty()).flatMapSingleElement(DyeingResult::fromRedis);
    }

    @Override
    public Completable update(Principal principal, String id, DyeingResultUpdateCommand command) {
        final Date currentDateTime = new Date();
        return dyeingPrepareRepository.find(id).flatMapCompletable(dyeingPrepare -> operatorRepository.find(principal).flatMapCompletable(operator ->
                Flowable.fromIterable(command.getItems()).flatMapSingle(item -> {
                    final DyeingResult dyeingResult = dyeingPrepare.getDyeingResults().stream()
                            .filter(it -> Objects.equals(item.getSilk().getId(), it.getSilk().getId()))
                            .findFirst().get();
                    return save(dyeingResult, item).flatMap(it -> {
                        it.log(operator, currentDateTime);
                        return dyeingResultRepository.save(it);
                    });
                }).toList().flatMap(it -> {
                    dyeingPrepare.setSubmitter(operator);
                    dyeingPrepare.setSubmitDateTime(currentDateTime);
                    return dyeingPrepareRepository.save(dyeingPrepare);
                }).ignoreElement()
        ));
    }

    private Single<DyeingResult> save(DyeingResult dyeingResult, DyeingResultUpdateCommand.Item item) throws Exception {
        dyeingResult.setHasException(item.isHasException());
        dyeingResult.formConfig(item.getFormConfig());
        dyeingResult.formConfigValueData(item.getFormConfigValueData());
        final Completable setGrade$ = Optional.ofNullable(item.getGrade())
                .map(EntityDTO::getId)
                .map(gradeRepository::find)
                .map(it -> it.doOnSuccess(dyeingResult::setGrade))
                .map(Single::ignoreElement)
                .orElseGet(() -> {
                    dyeingResult.setGrade(null);
                    return Completable.complete();
                });
        final Completable setSilkExceptions$ = Flowable.fromIterable(J.emptyIfNull(item.getSilkExceptions()))
                .map(EntityDTO::getId)
                .flatMapSingle(silkExceptionRepository::find).toList()
                .doOnSuccess(dyeingResult::setSilkExceptions)
                .ignoreElement();
        final Completable setSilkNotes$ = Flowable.fromIterable(J.emptyIfNull(item.getSilkNotes()))
                .map(EntityDTO::getId)
                .flatMapSingle(silkNoteRepository::find).toList()
                .doOnSuccess(dyeingResult::setSilkNotes)
                .ignoreElement();
        return Completable.mergeArray(setGrade$, setSilkExceptions$, setSilkNotes$)
                .andThen(Single.fromCallable(() -> dyeingResult));
    }

    @Override
    public Completable update(Principal principal, String id, String dyeingResultId, DyeingResultUpdateCommand.Item command) {
        return dyeingPrepareRepository.find(id).flatMapCompletable(dyeingPrepare -> {
            final DyeingResult dyeingResult = dyeingPrepare.getDyeingResults().stream()
                    .filter(it -> Objects.equals(it.getId(), dyeingResultId))
                    .findFirst().get();
            return save(dyeingResult, command).flatMap(it -> operatorRepository.find(principal).flatMap(operator -> {
                dyeingResult.log(operator);
                return dyeingResultRepository.save(dyeingResult);
            })).ignoreElement();
        });
    }

    @Override
    public Flowable<DyeingResult> listTimeline(String type, String currentId, String lineMachineId, int spindle, int size) {
        return lineMachineRepository.find(lineMachineId).flatMapMaybe(lineMachine -> {
            if (J.nonBlank(currentId)) {
                return dyeingResultRepository.find(currentId).toMaybe();
            }
            final String key;
            switch (type) {
                case "FIRST":
                    key = DyeingService.firstDyeingKey(lineMachine, spindle);
                    break;
                default:
                    key = DyeingService.crossDyeingKey(lineMachine, spindle);
                    break;
            }
            return rxLatest(key);
        }).flatMapPublisher(dyeingResult -> {
            final List<DyeingResult> dyeingResults = Lists.newArrayList(dyeingResult);
            DyeingResult prev = dyeingResult;
            for (int i = 0; i < size; i++) {
                prev = prev.getPrev();
                if (prev == null) {
                    break;
                }
                dyeingResults.add(prev);
            }
            return Flowable.fromIterable(dyeingResults);
        });
    }

}
