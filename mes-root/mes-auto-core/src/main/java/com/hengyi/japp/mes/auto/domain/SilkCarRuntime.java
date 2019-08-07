package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.PackageBoxEvent;
import com.hengyi.japp.mes.auto.application.event.SmallPackageBoxEvent;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.exception.SilkCarStatusException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 丝车运行时的状态，
 * 注意，为不可变对象，所有属性都不允许修改，只能增加
 *
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SilkCarRuntime implements Serializable {
    @ToString.Include
    @EqualsAndHashCode.Include
    private SilkCarRecord silkCarRecord;
    /**
     * 丝车最新的丝锭数据
     * 每次获取的时候实时运算
     */
    private Collection<SilkRuntime> silkRuntimes;

    private Collection<EventSource> eventSources;

    public Collection<Silk> silks() {
        return J.emptyIfNull(getSilkRuntimes()).stream()
                .map(SilkRuntime::getSilk)
                .collect(Collectors.toList());
    }

    public List<SilkRuntime> pickSilks(int count) throws SilkCarStatusException {
        final List<SilkRuntime> silks = getSilkRuntimes().stream().limit(count).collect(Collectors.toList());
        if (silks.size() == count) {
            return silks;
        }
        final SilkCarRecord silkCarRecord = getSilkCarRecord();
        final SilkCar silkCar = silkCarRecord.getSilkCar();
        throw new SilkCarStatusException(silkCar);
    }

    public boolean hasPackageBoxEvent() {
        return getEventSources().parallelStream()
                .filter(it -> !it.isDeleted() && (it instanceof PackageBoxEvent || it instanceof SmallPackageBoxEvent))
                .findAny().isPresent();
    }

    @JsonIgnore
    public boolean isBigSilkCar() {
        return DoffingType.BIG_SILK_CAR == getSilkCarRecord().getDoffingType();
    }
}
