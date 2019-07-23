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
public class MeasureFiberReportCommand implements Serializable {
    private String startDateTime;
    private String endDateTime;
    private String workshopId;

    @SneakyThrows
    public static MeasureFiberReportCommand measureFiberReportCommand(Object body) {
        JsonNode jsonNode = MAPPER.readTree((String) body);
        MeasureFiberReportCommand command = MAPPER.convertValue(jsonNode, MeasureFiberReportCommand.class);
        return command;
    }
}
