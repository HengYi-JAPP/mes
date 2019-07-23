package com.hengyi.japp.mes.auto.report.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.SneakyThrows;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author liuyuan
 * @create 2019-06-30 11:35
 * @description
 **/
@Data
public class PackagePlanCommand {
    private String workshopId;

    @SneakyThrows
    public static PackagePlanCommand packagePlanCommand(Object body) {
        JsonNode jsonNode = MAPPER.readTree((String) body);
        PackagePlanCommand command = MAPPER.convertValue(jsonNode, PackagePlanCommand.class);
        return command;
    }
}
