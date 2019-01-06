package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.DyeingSampleRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;


/**
 * 工序操作数据提交
 *
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DyeingSampleSubmitEvent extends EventSource {
    @NotNull
    @Size(min = 1)
    private Collection<SilkRuntime> silkRuntimes;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        data = Sets.newHashSet(J.emptyIfNull(data));
        data.removeAll(J.emptyIfNull(silkRuntimes));
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        final DyeingSampleRepository dyeingSampleRepository = Jvertx.getProxy(DyeingSampleRepository.class);
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

        return Flowable.fromIterable(J.emptyIfNull(silkRuntimes))
                .map(SilkRuntime::getSilk).toList()
                .flatMapCompletable(silks -> {
                    silks.forEach(silk -> {
                        if (!silk.isDyeingSample()) {
                            throw new RuntimeException("丝锭silk[" + silk.getCode() + "]处于非标样丝状态");
                        }
                        silk.setDyeingSample(false);
                    });

                    return Flowable.fromIterable(silks).map(Silk::getId).flatMapSingle(dyeingSampleRepository::find).toList().flatMapCompletable(dyeingSamples -> {
                        dyeingSamples.forEach(dyeingSample -> {
                            if (dyeingSample.isUsed()) {
                                throw new RuntimeException("标样丝[" + dyeingSample.getCode() + "]已经使用");
                            }
                            dyeingSample.setDeleted(true);
                            dyeingSample.log(operator);
                        });
                        return Completable.mergeArray(
                                Flowable.fromIterable(dyeingSamples).flatMapSingle(dyeingSampleRepository::save).toList().ignoreElement(),
                                Flowable.fromIterable(silks).flatMapSingle(silkRepository::save).toList().ignoreElement()
                        );
                    });
                });
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.DyeingSampleSubmitEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends EventSource.DTO {
        private Collection<SilkRuntime.DTO> silkRuntimes;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<DyeingSampleSubmitEvent> toEvent() {
            final DyeingSampleSubmitEvent event = new DyeingSampleSubmitEvent();
            return Flowable.fromIterable(silkRuntimes)
                    .flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList()
                    .flatMap(silkRuntimes -> {
                        event.setSilkRuntimes(silkRuntimes);
                        return toEvent(event);
                    });
        }
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        @Size(min = 1)
        private Collection<SilkRuntime.DTO> silkRuntimes;

        public Single<DyeingSampleSubmitEvent> toEvent(Principal principal) {
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final DyeingSampleSubmitEvent event = new DyeingSampleSubmitEvent();
            return Flowable.fromIterable(silkRuntimes)
                    .flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList()
                    .flatMap(silkRuntimes -> {
                        event.setSilkRuntimes(silkRuntimes);
                        return operatorRepository.find(principal);
                    }).map(it -> {
                        event.fire(it);
                        return event;
                    });
        }
    }
}
