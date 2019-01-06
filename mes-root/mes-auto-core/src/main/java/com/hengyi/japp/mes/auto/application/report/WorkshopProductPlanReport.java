package com.hengyi.japp.mes.auto.application.report;

import com.hengyi.japp.mes.auto.domain.*;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzb 2018-08-12
 */
@Data
public class WorkshopProductPlanReport implements Serializable {
    private final Workshop workshop;
    private final Collection<Item> items;

    public WorkshopProductPlanReport(List<LineMachine> lineMachines) {
        items = lineMachines.stream()
                .collect(Collectors.groupingBy(LineMachine::getLine))
                .entrySet()
                .stream()
                .flatMap(this::calcItems)
                .collect(Collectors.toList());
        workshop = items.stream()
                .findFirst()
                .map(Item::getLine)
                .map(Line::getWorkshop)
                .orElse(null);
    }

    private Stream<Item> calcItems(Map.Entry<Line, List<LineMachine>> entry) {
        final Line line = entry.getKey();
        final List<LineMachine> lineMachines = entry.getValue();
        return lineMachines.stream()
                .collect(Collectors.groupingBy(it -> Optional.ofNullable(it)
                        .map(LineMachine::getProductPlan)
                        .map(LineMachineProductPlan::getBatch)
                        .map(Batch::getBatchNo)
                        .orElse(""))
                )
                .entrySet()
                .stream()
                .map(it -> calcItems(line, it))
                .filter(Objects::nonNull);
    }

    private Item calcItems(Line line, Map.Entry<String, List<LineMachine>> entry) {
        final Item item = new Item();
        item.setLine(line);
        final List<LineMachine> lineMachines = entry.getValue();
        item.setLineMachines(lineMachines);

        final String batchNo = entry.getKey();
        final Batch batch = CollectionUtils.emptyIfNull(lineMachines)
                .stream()
                .map(LineMachine::getProductPlan)
                .filter(Objects::nonNull)
                .map(LineMachineProductPlan::getBatch)
                .filter(it -> Objects.equals(it.getBatchNo(), batchNo))
                .findFirst()
                .orElse(null);
        item.setBatch(batch);

        return item;
    }

    @Data
    public static class Item implements Serializable {
        private Line line;
        private Batch batch;
        private Collection<LineMachine> lineMachines;
    }

}
