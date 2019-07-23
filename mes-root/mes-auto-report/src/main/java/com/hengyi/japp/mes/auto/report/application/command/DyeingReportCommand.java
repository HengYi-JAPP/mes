package com.hengyi.japp.mes.auto.report.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author liuyuan
 * @create 2019-06-05 8:17
 * @description
 **/
@Data
public class DyeingReportCommand implements Serializable {

    private String startDateTime;
    private String endDateTime;
    private String workshopId;

    @SneakyThrows
    public static DyeingReportCommand dyeingReportCommand(Object body) {
        JsonNode jsonNode = MAPPER.readTree((String) body);
        DyeingReportCommand command = MAPPER.convertValue(jsonNode, DyeingReportCommand.class);
        return command;
    }
}
