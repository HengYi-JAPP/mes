package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.domain.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.ProductProcessRepository;
import com.hengyi.japp.mes.auto.repository.SilkExceptionRepository;
import com.hengyi.japp.mes.auto.repository.SilkNoteRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
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
public class ProductProcessSubmitEvent extends EventSource {
    private ProductProcess productProcess;
    private Collection<SilkRuntime> silkRuntimes;
    private Collection<SilkException> silkExceptions;
    private Collection<SilkNote> silkNotes;
    private JsonNode formConfig;
    private JsonNode formConfigValueData;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        J.emptyIfNull(data).stream()
                .filter(J.emptyIfNull(silkRuntimes)::contains)
                .forEach(it -> {
                    it.addExceptions(silkExceptions);
                    it.addNotes(silkNotes);
                });
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        return Completable.complete();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.ProductProcessSubmitEvent;
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
        private EntityDTO productProcess;
        private Collection<SilkRuntime.DTO> silkRuntimes;
        private Collection<EntityDTO> silkExceptions;
        private Collection<EntityDTO> silkNotes;
        private JsonNode formConfig;
        private JsonNode formConfigValueData;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<ProductProcessSubmitEvent> toEvent() {
            final ProductProcessRepository productProcessRepository = Jvertx.getProxy(ProductProcessRepository.class);
            final SilkExceptionRepository silkExceptionRepository = Jvertx.getProxy(SilkExceptionRepository.class);
            final SilkNoteRepository silkNoteRepository = Jvertx.getProxy(SilkNoteRepository.class);

            final ProductProcessSubmitEvent event = new ProductProcessSubmitEvent();
            event.setFormConfig(formConfig);
            event.setFormConfig(formConfigValueData);
            return productProcessRepository.find(productProcess.getId()).flatMap(productProcess -> {
                event.setProductProcess(productProcess);
                return Flowable.fromIterable(J.emptyIfNull(silkExceptions)).map(EntityDTO::getId).flatMapSingle(silkExceptionRepository::find).toList();
            }).flatMap(silkExceptions -> {
                event.setSilkExceptions(silkExceptions);
                return Flowable.fromIterable(J.emptyIfNull(silkNotes)).map(EntityDTO::getId).flatMapSingle(silkNoteRepository::find).toList();
            }).flatMap(silkNotes -> {
                event.setSilkNotes(silkNotes);
                return Flowable.fromIterable(J.emptyIfNull(silkRuntimes)).flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList();
            }).flatMap(silkRuntimes -> {
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
        private EntityDTO productProcess;
        private Collection<SilkRuntime.DTO> silkRuntimes;
        private Collection<EntityDTO> silkExceptions;
        private Collection<EntityDTO> silkNotes;
        private JsonNode formConfig;
        private JsonNode formConfigValueData;

        public Single<ProductProcessSubmitEvent> toEvent(Principal principal) {
            final ProductProcessRepository productProcessRepository = Jvertx.getProxy(ProductProcessRepository.class);
            final SilkExceptionRepository silkExceptionRepository = Jvertx.getProxy(SilkExceptionRepository.class);
            final SilkNoteRepository silkNoteRepository = Jvertx.getProxy(SilkNoteRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final ProductProcessSubmitEvent event = new ProductProcessSubmitEvent();
            event.setFormConfig(formConfig);
            event.setFormConfig(formConfigValueData);
            return productProcessRepository.find(productProcess.getId()).flatMap(productProcess -> {
                event.setProductProcess(productProcess);
                return Flowable.fromIterable(J.emptyIfNull(silkRuntimes)).flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList();
            }).flatMap(silkRuntimes -> {
                event.setSilkRuntimes(silkRuntimes);
                return Flowable.fromIterable(J.emptyIfNull(silkExceptions)).map(EntityDTO::getId).flatMapSingle(silkExceptionRepository::find).toList();
            }).flatMap(silkExceptions -> {
                event.setSilkExceptions(silkExceptions);
                return Flowable.fromIterable(J.emptyIfNull(silkNotes)).map(EntityDTO::getId).flatMapSingle(silkNoteRepository::find).toList();
            }).flatMap(silkNotes -> {
                event.setSilkNotes(silkNotes);
                return operatorRepository.find(principal);
            }).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }
}
