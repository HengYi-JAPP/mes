package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.exception.JAuthorizationException;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * 事件源
 *
 * @author jzb 2018-07-29
 */
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class EventSource implements Serializable, Comparable<EventSource> {
    @ToString.Include
    @EqualsAndHashCode.Include
    private String eventId;
    private Operator operator;
    private Date fireDateTime;
    private boolean deleted;
    private Operator deleteOperator;
    private Date deleteDateTime;

    public static Single<EventSource> from(JsonNode jsonNode) {
        return Single.fromCallable(() -> jsonNode.get("type"))
                .map(JsonNode::asText)
                .map(EventSourceType::valueOf)
                .flatMap(it -> it.from(jsonNode));
    }

    public static Single<EventSource> from(File file) {
        return Single.just(file).map(MAPPER::readTree).flatMap(EventSource::from);
    }

    public Collection<SilkRuntime> calcSilkRuntimes(Collection<SilkRuntime> data) {
        if (deleted) {
            return data;
        }
        return _calcSilkRuntimes(data);
    }

    protected abstract Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data);

    public Single<EventSource> undo(Principal principal) {
        final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);
        return operatorRepository.find(principal).flatMap(this::undo);
    }

    public Single<EventSource> undo(Operator operator) throws JAuthorizationException {
        if (!operator.isAdmin() && !Objects.equals(this.getOperator(), operator)) {
            throw new JAuthorizationException();
        }
        return _undo(operator).andThen(Single.fromCallable(() -> {
            this.deleted = true;
            this.deleteOperator = operator;
            this.deleteDateTime = new Date();
            return this;
        }));
    }

    protected abstract Completable _undo(Operator operator);

    @ToString.Include
    public abstract EventSourceType getType();

    public abstract JsonNode toJsonNode();

    @Override
    public int compareTo(EventSource o) {
        return this.fireDateTime.compareTo(o.fireDateTime);
    }

    public void fire(Operator operator) {
        this.eventId = new ObjectId().toHexString();
        this.operator = operator;
        this.operator.setId(operator.getId());
        this.fireDateTime = new Date();
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static abstract class DTO implements Serializable {
        @ToString.Include
        @EqualsAndHashCode.Include
        private String eventId;
        private EventSourceType type;
        private EntityDTO operator;
        private Date fireDateTime;
        private boolean deleted;
        private EntityDTO deleteOperator;
        private Date deleteDateTime;

        protected <T extends EventSource> Single<T> toEvent(T t) {
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);
            t.setEventId(eventId);
            t.setFireDateTime(fireDateTime);
            t.setDeleted(deleted);
            t.setDeleteDateTime(deleteDateTime);
            return operatorRepository.find(operator.getId()).flatMap(operator -> {
                t.setOperator(operator);
                if (!deleted) {
                    return Single.just(t);
                }
                return operatorRepository.find(deleteOperator.getId()).map(it2 -> {
                    t.setDeleteOperator(it2);
                    return t;
                });
            });
        }
    }

}
