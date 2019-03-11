package com.hengyi.japp.mes.auto.interfaces.ruiguan.internal;

import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.interfaces.ruiguan.RuiguanService;
import io.reactivex.Completable;

import java.security.Principal;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-03-11
 */
@Singleton
public class RuiguanServiceImpl implements RuiguanService {
    @Override
    public Completable handle(Principal principal, SilkCarRuntimeInitEvent.RuiguanAutoDoffingCommand command) {
        return Completable.complete()
                .doOnComplete(() -> {
                    final StringBuilder sb = new StringBuilder("RiambResource.fetchSilkCarRecord: ")
                            .append(MAPPER.writeValueAsString(command))
                            .append("\n").append("成功,SilkCarRecord[" + "" + "]");
                    LOG.info(sb.toString());
                })
                .doOnError(ex -> {
                });
    }
}
