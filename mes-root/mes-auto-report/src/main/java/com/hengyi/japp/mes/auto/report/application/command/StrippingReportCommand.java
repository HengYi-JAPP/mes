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
public class StrippingReportCommand implements Serializable {
    private String startDate;
    private String endDate;
    private String workshopId;

    @SneakyThrows
    public static StrippingReportCommand strippingReportCommand(Object body) {
        JsonNode jsonNode = MAPPER.readTree((String) body);
        StrippingReportCommand command = MAPPER.convertValue(jsonNode, StrippingReportCommand.class);
        return command;
    }
}
