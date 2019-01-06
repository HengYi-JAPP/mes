package com.hengyi.japp.mes.auto.application.report;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Silk;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-11-26
 */
@Data
public class DoffingReport implements Serializable {
    private final Collection<Item> items;

    public DoffingReport(Collection<Silk> silks) {
        this.items = J.emptyIfNull(silks).stream()
                .collect(Collectors.groupingBy(Silk::getBatch))
                .entrySet()
                .stream()
                .map(entry -> {
                    final Batch batch = entry.getKey();
                    final List<Silk> silkList = entry.getValue();
                    return new Item(batch, silkList);
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class Item implements Serializable {
        private final Batch batch;
        private final Collection<Silk> silks;

        public Item(Batch batch, Collection<Silk> silks) {
            this.batch = batch;
            this.silks = silks;
        }
    }
}
