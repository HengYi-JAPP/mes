package com.hengyi.japp.mes.auto.domain;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 线别
 *
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Line extends LoggableMongoEntity {
    private Workshop workshop;
    @ToString.Include
    private String name;
    private DoffingType doffingType;

    public Flowable<LineMachine> lineMachines() {
        final LineMachineRepository lineMachineRepository = Jvertx.getProxy(LineMachineRepository.class);
        return lineMachineRepository.listBy(this);
    }

}
