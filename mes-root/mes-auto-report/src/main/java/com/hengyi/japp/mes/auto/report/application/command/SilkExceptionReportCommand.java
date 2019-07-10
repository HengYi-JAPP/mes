package com.hengyi.japp.mes.auto.report.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author liuyuan
 * @create 2019-05-30 10:43
 * @description
 **/
@Data
public class SilkExceptionReportCommand implements Serializable {
    private long startDateTime;
    private long endDateTime;
    private String workshopId;

    @SneakyThrows
    public static SilkExceptionReportCommand silkExceptionReportCommand(Object body) {
        JsonNode jsonNode = MAPPER.readTree((String) body);
        SilkExceptionReportCommand command = MAPPER.convertValue(jsonNode, SilkExceptionReportCommand.class);
        return command;
    }
}
