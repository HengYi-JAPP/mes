package com.hengyi.japp.mes.auto.search;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.GuiceModule;
import io.vertx.reactivex.core.Vertx;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author jzb 2018-03-21
 */
public class SearchModule extends GuiceModule {

    public SearchModule(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    @Named("luceneRootPath")
    private Path luceneRootPath(@Named("autoRootPath") Path autoRootPath) {
        final Path path = Paths.get("db", "lucene");
        return autoRootPath.resolve(path);
    }

}
