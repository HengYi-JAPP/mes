package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.Collection;

/**
 * 通知
 *
 * @author jzb 2018-06-21
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Notification extends LoggableMongoEntity {
    private Collection<Workshop> workshops;
    private Collection<Line> lines;
    @NotBlank
    private String note;

    @JsonGetter("modifier")
    public Operator _modifier_() {
        return getModifier();
    }

}
