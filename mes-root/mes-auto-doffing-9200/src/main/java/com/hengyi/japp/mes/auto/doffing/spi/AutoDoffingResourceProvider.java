package com.hengyi.japp.mes.auto.doffing.spi;

import com.github.ixtf.vertx.jax_rs.spi.ResourceProviderJaxRs;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.hengyi.japp.mes.auto.doffing.Doffing.INJECTOR;

/**
 * @author jzb 2018-11-01
 */
public class AutoDoffingResourceProvider extends ResourceProviderJaxRs {

    @Override
    public String addressPrefix() {
        return "mes-auto-doffing";
    }

    @Override
    protected Set<String> getPackages() {
        return Sets.newHashSet("com.hengyi.japp.mes.auto.doffing.rest");
    }

    @Override
    protected Set<Class> getClasses() {
        return null;
    }

    @Override
    public <T> T getProxy(Class<T> clazz) {
        return INJECTOR.getInstance(clazz);
    }
}
