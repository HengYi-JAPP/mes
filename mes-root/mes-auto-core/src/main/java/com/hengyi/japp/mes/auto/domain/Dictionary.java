package com.hengyi.japp.mes.auto.domain;

import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author liuyuan
 * @create 2019-03-14 14:06
 * @description
 **/
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Dictionary extends LoggableMongoEntity{
    @ToString.Include
    private String key;
    @ToString.Include
    private String value;
}
