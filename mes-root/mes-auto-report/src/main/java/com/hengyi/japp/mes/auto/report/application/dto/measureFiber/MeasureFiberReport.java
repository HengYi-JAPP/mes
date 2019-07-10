package com.hengyi.japp.mes.auto.report.application.dto.measureFiber;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.bson.Document;

import java.io.Serializable;
import java.util.List;

/**
 * @author jzb 2018-08-12
 */
@Data
public class MeasureFiberReport implements Serializable {

    private final List<Item> items;

    public MeasureFiberReport(List<Item> items) {
        this.items = items;
    }

    @Data
    public static class Item implements Serializable {

        private final List<ObjectNode> eventSources;
        private final Document silkCarRecord;
        private final Document product;
        private final int silkCount;
    }
}
