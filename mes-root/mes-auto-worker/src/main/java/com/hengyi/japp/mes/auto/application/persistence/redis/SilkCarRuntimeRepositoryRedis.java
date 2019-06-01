package com.hengyi.japp.mes.auto.application.persistence.redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.DyeingService;
import com.hengyi.japp.mes.auto.application.event.DyeingPrepareEvent;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordRepository;
import com.hengyi.japp.mes.auto.repository.SilkCarRuntimeRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-11-08
 */
@Slf4j
@Singleton
public class SilkCarRuntimeRepositoryRedis implements SilkCarRuntimeRepository {
    private final RedisClient redisClient;
    private final SilkCarRecordRepository silkCarRecordRepository;
    private final SilkRepository silkRepository;

    @Inject
    private SilkCarRuntimeRepositoryRedis(RedisClient redisClient, SilkCarRecordRepository silkCarRecordRepository, SilkRepository silkRepository) {
        this.redisClient = redisClient;
        this.silkCarRecordRepository = silkCarRecordRepository;
        this.silkRepository = silkRepository;
    }

    @Override
    public Single<SilkCarRuntime> create(SilkCarRecord silkCarRecord_, Collection<SilkRuntime> silkRuntimes_) {
        return silkCarRecordRepository.save(silkCarRecord_).flatMap(silkCarRecord -> {
            /**
             * 保存丝锭
             * 注意：先保存 silkCarRecord
             */
            return Flowable.fromIterable(silkRuntimes_).flatMapSingle(silkRuntime -> {
                final Silk silk_ = silkRuntime.getSilk();
                // 考虑到拼车情况，
                Collection<SilkCarRecord> silkCarRecords = J.emptyIfNull(silk_.getSilkCarRecords());
                silkCarRecords = Lists.newArrayList(silkCarRecords);
                if (!silkCarRecords.contains(silkCarRecord)) {
                    silkCarRecords.add(silkCarRecord);
                }
                silk_.setSilkCarRecords(silkCarRecords);
                return silkRepository.save(silk_).map(silk -> {
                    silkRuntime.setSilk(silk);
                    return silkRuntime;
                });
            }).toList().map(silkRuntimes -> {
                final SilkCarRuntime silkCarRuntime = new SilkCarRuntime();
                silkCarRuntime.setSilkCarRecord(silkCarRecord);
                silkCarRuntime.setSilkRuntimes(silkRuntimes);
                return silkCarRuntime;
            });
        }).flatMap(silkCarRuntime -> {
            final String redisKey = SilkCarRuntimeRepository.redisKey(silkCarRuntime);
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            final JsonObject redisJson = new JsonObject().put("silkCarRecord", silkCarRecord.getId());
            final Completable save$ = redisClient.rxHmset(redisKey, redisJson).ignoreElement();
            return save$.toSingleDefault(silkCarRuntime);
        });
    }

    @Override
    public Completable clearSilkCarRuntime(String code) {
        return redisClient.rxDel(SilkCarRuntimeRepository.redisKey(code)).ignoreElement();
    }

    @Override
    public Maybe<SilkCarRuntime> findByCode(String code) {
        return redisClient.rxHgetall(SilkCarRuntimeRepository.redisKey(code))
                .filter(it -> !it.isEmpty())
                .flatMapSingleElement(this::fromRedisJson);
    }

    private Single<SilkCarRuntime> fromRedisJson(JsonObject redisJson) {
        final SilkCarRuntime silkCarRuntime = new SilkCarRuntime();
        return silkCarRecordRepository.find(redisJson.getString("silkCarRecord")).flatMap(silkCarRecord -> {
            silkCarRuntime.setSilkCarRecord(silkCarRecord);
            return Flowable.fromIterable(redisJson.fieldNames())
                    .filter(it -> it.startsWith(EVENT_SOURCE_KEY_PREFIX))
                    .map(redisJson::getString)
                    .map(MAPPER::readTree)
                    .flatMapSingle(EventSource::from).sorted().toList()
                    .flatMap(eventSources -> {
                        silkCarRuntime.setEventSources(eventSources);
                        Collection<SilkRuntime> silkRuntimes = silkCarRecord.initSilks();
                        J.emptyIfNull(silkRuntimes).forEach(silkRuntime -> {
                            final Grade grade = silkCarRecord.getGrade();
                            silkRuntime.setGrade(grade);
                        });

                        final Collection<DyeingResult> selfDyeingResults = Lists.newArrayList();
                        for (EventSource eventSource : eventSources) {
                            silkRuntimes = eventSource.calcSilkRuntimes(silkRuntimes);
                            if (eventSource.getType() == EventSourceType.DyeingPrepareEvent) {
                                final DyeingPrepareEvent dyeingPrepareEvent = (DyeingPrepareEvent) eventSource;
                                final DyeingPrepare dyeingPrepare = dyeingPrepareEvent.getDyeingPrepare();
                                selfDyeingResults.addAll(J.emptyIfNull(dyeingPrepare.getDyeingResults()));
                            }
                        }
                        J.emptyIfNull(silkRuntimes).forEach(silkRuntime -> {
                            silkRuntime.getDyeingResultCalcModel().setSelfDyeingResults(selfDyeingResults);
                            if (silkRuntime.getGrade() == null) {
                                // 拼车没有预设等级，沿用丝车的等级
                                final Grade grade = silkCarRecord.getGrade();
                                silkRuntime.setGrade(grade);
                            }
                        });

                        final Batch batch = silkCarRecord.getBatch();
                        final Product product = batch.getProduct();
                        if (product.getDyeingFormConfig() == null) {
                            silkCarRuntime.setSilkRuntimes(silkRuntimes);
                            return Single.fromCallable(() -> silkCarRuntime);
                        }

                        final Flowable<SilkRuntime> silkRuntimeFlowable;
                        if (silkCarRecord.getDoffingType() != null) {
                            silkRuntimeFlowable = Flowable.fromIterable(silkRuntimes).flatMapSingle(this::calcTimelineDyeingException);
                        } else {
                            silkRuntimeFlowable = Flowable.fromIterable(silkRuntimes);
                        }

                        return silkRuntimeFlowable.doOnNext(silkRuntime -> {
                            if (silkRuntime.getGrade() == null) {
                                // 拼车没有预设等级，沿用丝车的等级
                                silkRuntime.setGrade(silkCarRecord.getGrade());
                            }
                        }).toList().map(it -> {
                            it.forEach(SilkRuntime::calcDyeing);
                            silkCarRuntime.setSilkRuntimes(it);
                            return silkCarRuntime;
                        });
                    });
        });
    }

    private Single<SilkRuntime> calcTimelineDyeingException(SilkRuntime silkRuntime) {
        // 这丝本身发生过织袜
//        if (silkRuntime.getSelfDyeingResultInfo() != null) {
//            return Single.just(silkRuntime);
//        }
//        if (J.nonEmpty(silkRuntime.getDyeingResultCalcModel().getDyeingResults())) {
//            return Single.just(silkRuntime);
//        }
        final Silk silk = silkRuntime.getSilk();
        final LineMachine lineMachine = silk.getLineMachine();
        final int spindle = silk.getSpindle();
        final String firstDyeingKey = DyeingService.firstDyeingKey(lineMachine, spindle);

        return calcTimelineDyeingException(firstDyeingKey, silkRuntime).flatMap(it -> {
            final String crossDyeingKey = DyeingService.crossDyeingKey(lineMachine, spindle);
            return calcTimelineDyeingException(crossDyeingKey, silkRuntime);
        });
    }

    private Single<SilkRuntime> calcTimelineDyeingException(String key, SilkRuntime silkRuntime) {
        return redisClient.rxHgetall(key).flatMap(it -> {
            if (it.isEmpty()) {
//                silkRuntime.setDyeingResultSubmitted(false);
//                silkRuntime.setHasDyeingException(true);
                return Single.just(silkRuntime);
            }
            return DyeingResult.fromRedis(it).map(dyeingResult -> dyeingResult.fillData(silkRuntime));
        });
    }

    @Override
    public Completable addEventSource(String code, EventSource eventSource) {
        return Single.fromCallable(() -> {
            final String eventSourceKey = EVENT_SOURCE_KEY_PREFIX + eventSource.getEventId();
            final JsonNode jsonNode = eventSource.toJsonNode();
            final String eventSourceValue = MAPPER.writeValueAsString(jsonNode);
            return new JsonObject().put(eventSourceKey, eventSourceValue);
        }).flatMapCompletable(it -> {
            final String redisKey = SilkCarRuntimeRepository.redisKey(code);
            return redisClient.rxHmset(redisKey, it).ignoreElement();
        });
    }

    @Override
    public Completable delete(SilkCarRuntime silkCarRuntime) {
        final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
        final SilkCar silkCar = silkCarRecord.getSilkCar();
        final String redisKey = SilkCarRuntimeRepository.redisKey(silkCar.getCode());
        final Completable delSilkCarRecord$ = silkCarRecordRepository.delete(silkCarRecord);
        final Completable delSilkCarRuntime$ = redisClient.rxDel(redisKey).ignoreElement();
        return delSilkCarRecord$.andThen(delSilkCarRuntime$);
    }
}
