package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 车间异常处理记录
 *
 * @author jzb 2018-06-21
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ExceptionRecord extends LoggableMongoEntity {
    @NotNull
    private LineMachine lineMachine;
    @Min(1)
    private int spindle;
    @NotBlank
    private String doffingNum;
    @NotNull
    private SilkException exception;
    private Silk silk;
    private boolean handled;
    private Operator handler;
    private Date handleDateTime;

    @JsonGetter("creator")
    public Operator _creator_() {
        return getCreator();
    }

}
