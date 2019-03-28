package com.hengyi.japp.mes.auto.domain;

import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author liuyuan
 * @create 2019-03-14 14:06
 * @description
 **/
@Data
@NoArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Dictionary extends LoggableMongoEntity implements Comparable<Dictionary>{
    @ToString.Include
    private String key;
    @ToString.Include
    private String value;

    @Override
    public int compareTo(Dictionary o) {
        return ComparisonChain.start().compare(key,o.key).result();
    }
}
