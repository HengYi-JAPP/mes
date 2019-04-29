package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.Constant;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;

import java.util.Set;
import java.util.stream.Collectors;

public class SilkCarRuntimePackagedException extends JException {
    private final Set<SilkCarRuntime> silkCarRuntimes;

    public SilkCarRuntimePackagedException(Set<SilkCarRuntime> silkCarRuntimes) {
        super(Constant.ErrorCode.SILK_CAR_RUNTIME_PACKAGED);
        this.silkCarRuntimes = silkCarRuntimes;
    }

    public SilkCarRuntimePackagedException(SilkCarRuntime silkCarRuntime) {
        this(Sets.newHashSet(silkCarRuntime));
    }

    @Override
    public String getMessage() {
        final String silkCarCodeString = silkCarRuntimes.stream()
                .map(SilkCarRuntime::getSilkCarRecord)
                .map(SilkCarRecord::getSilkCar)
                .map(SilkCar::getCode)
                .collect(Collectors.joining(","));
        return "[" + silkCarCodeString + "]，已经被打包";
    }
}
