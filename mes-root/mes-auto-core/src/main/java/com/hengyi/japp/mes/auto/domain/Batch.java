package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.interfaces.jackson.ProductEmbedSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 批号
 *
 * @author jzb 2018-06-21
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Batch extends LoggableMongoEntity implements Comparable<Batch> {
    private Workshop workshop;
    @JsonSerialize(using = ProductEmbedSerializer.class)
    @ToString.Include
    private Product product;
    @ToString.Include
    private String batchNo;
    private double silkWeight;
    private double centralValue;
    private int holeNum;
    /**
     * 规格
     */
    @ToString.Include
    private String spec;
    private String tubeColor;
    private String note;

    @Override
    public int compareTo(Batch o) {
        return ComparisonChain.start()
                .compare(batchNo, o.batchNo)
                .result();
    }
}
