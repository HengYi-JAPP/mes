package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.data.SilkCarRecordAggregateType;
import com.hengyi.japp.mes.auto.report.application.RedisService;
import com.hengyi.japp.mes.auto.repository.SilkCarRuntimeRepository;
import org.bson.Document;

import java.util.Collection;
import java.util.Map;

import static com.hengyi.japp.mes.auto.repository.SilkCarRuntimeRepository.EVENT_SOURCE_KEY_PREFIX;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-07-11
 */
public class SilkCarRecordAggregate_Runtime extends SilkCarRecordAggregate {

    protected SilkCarRecordAggregate_Runtime(Document document) {
        super(document);
    }

    @Override
    protected Collection<EventSource.DTO> fetchEventSources() {
        final String code = silkCar.getString("code");
        final String redisKey = SilkCarRuntimeRepository.redisKey(code);
        final Map<String, String> redisMap = RedisService.call(jedis -> jedis.hgetAll(redisKey));
        return redisMap.keySet().parallelStream()
                .filter(it -> it.startsWith(EVENT_SOURCE_KEY_PREFIX))
                .map(redisMap::get)
                .map(this::toEventSource)
                .collect(toList());
    }

    @Override
    public SilkCarRecordAggregateType getType() {
        return SilkCarRecordAggregateType.RUNTIME;
    }

}
