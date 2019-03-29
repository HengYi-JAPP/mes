package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.Dictionary;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

/**
 * @author liuyuan
 * @create 2019-03-14 15:03
 * @description
 **/
@Builder
public class DictionaryQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<Dictionary> dictionaries;
    }
}
