package com.hengyi.japp.mes.auto.interfaces.search.internal;

import com.github.ixtf.japp.core.J;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jzb 2019-11-14
 */
@Data
public class QueryResult implements Serializable {
    private long count;
    private Collection<String> ids;


    public Pair<Long, Collection<String>> pair() {
        return Pair.of(count, J.emptyIfNull(ids));
    }
}
