package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.repository.DyeingResultRepository;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-08-02
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DyeingResult extends LoggableMongoEntity {
    @JsonIgnore
    private DyeingPrepare dyeingPrepare;
    private Silk silk;

    @ToString.Include
    private LineMachine lineMachine;
    @ToString.Include
    private int spindle;
    @ToString.Include
    private Date dateTime;

    private boolean hasException;
    private Grade grade;
    private Collection<SilkException> silkExceptions;
    private Collection<SilkNote> silkNotes;
    @JsonIgnore
    @JsonEntityProperty("formConfig")
    private String formConfigJsonString;
    @JsonIgnore
    @JsonEntityProperty("formConfigValueData")
    private String formConfigValueDataJsonString;
    @JsonIgnore
    private DyeingResult prev;
    @JsonIgnore
    private DyeingResult next;

    public static Single<DyeingResult> fromRedis(JsonObject redisJson) {
        final DyeingResultRepository dyeingResultRepository = Jvertx.getProxy(DyeingResultRepository.class);
        return dyeingResultRepository.find(redisJson.getString("id"));
    }

    public boolean isSubmitted() {
        return getDyeingPrepare().isSubmitted();
    }

    @JsonIgnore
    public boolean isFirst() {
        return getDyeingPrepare().isFirst();
    }

    @JsonIgnore
    public boolean isCross() {
        return getDyeingPrepare().isCross();
    }

    @JsonIgnore
    public boolean isMulti() {
        return getDyeingPrepare().isMulti();
    }

    @JsonGetter("formConfig")
    public JsonNode formConfig() throws IOException {
        final String jsonString = getFormConfigJsonString();
        return J.nonBlank(jsonString) ? MAPPER.readTree(jsonString) : null;
    }

    public void formConfig(JsonNode formConfig) throws JsonProcessingException {
        if (formConfig == null) {
            setFormConfigJsonString(null);
        } else {
            setFormConfigJsonString(MAPPER.writeValueAsString(formConfig));
        }
    }

    @JsonGetter("formConfigValueData")
    public JsonNode formConfigValueData() throws IOException {
        final String jsonString = getFormConfigValueDataJsonString();
        return J.nonBlank(jsonString) ? MAPPER.readTree(jsonString) : null;
    }

    public void formConfigValueData(JsonNode node) throws JsonProcessingException {
        if (node == null) {
            setFormConfigValueDataJsonString(null);
        } else {
            setFormConfigValueDataJsonString(MAPPER.writeValueAsString(node));
        }
    }

    public JsonObject toRedisJsonObject() {
        return new JsonObject().put("id", getId())
                .put("dyeingPrepare", getDyeingPrepare().getId());
    }

    public List<DyeingResult> linkPush(DyeingResult item) {
        if (Objects.equals(this, item)) {
            return Lists.newArrayList(item);
        }
        final DyeingResult prev = getPrev();
        final DyeingResult next = getNext();

        if (prev != null && next != null) {
            if (item.getDateTime().getTime() >= prev.getDateTime().getTime() && item.getDateTime().getTime() <= getDateTime().getTime()) {
                final List<DyeingResult> result = Lists.newArrayList(this, prev, item);
                item.setNext(this);
                item.setPrev(prev);
                prev.setNext(item);
                setPrev(item);
                return result;
            }
        }

        if (prev == null && next == null) {
            if (item.getDateTime().getTime() >= getDateTime().getTime()) {
                setNext(item);
                item.setPrev(this);
            } else {
                setPrev(item);
                item.setNext(this);
            }
            return Lists.newArrayList(this, item);
        }

        if (item.getDateTime().getTime() >= getDateTime().getTime()) {
            if (next == null) {
                setNext(item);
                item.setPrev(this);
                return Lists.newArrayList(this, item);
            } else {
                return next.linkPush(item);
            }
        } else {
            if (prev == null) {
                setPrev(item);
                item.setNext(this);
                return Lists.newArrayList(this, item);
            } else {
                return prev.linkPush(item);
            }
        }
    }

    public SilkRuntime fillData(SilkRuntime silkRuntime) {
        final Silk silk = silkRuntime.getSilk();
        if (silk.getDoffingDateTime().getTime() >= getDateTime().getTime()) {
            silkRuntime.getDyeingResultCalcModel().add(this);
            return silkRuntime;
        }
        final DyeingResult prev = getPrev();
        if (prev != null) {
            return prev.fillData(silkRuntime);
        }
        return silkRuntime;
    }

}
