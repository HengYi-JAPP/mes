package com.hengyi.japp.mes.auto.report.application.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.SneakyThrows;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;

/**
 * @author jzb 2019-05-20
 */
@Singleton
public class QueryServiceImpl implements QueryService {
    private final MesAutoConfig mesAutoConfig;

    @Inject
    private QueryServiceImpl(MesAutoConfig mesAutoConfig) {
        this.mesAutoConfig = mesAutoConfig;
    }

    @SneakyThrows
    @Override
    public IndexReader indexReader(Class clazz) {
        final Path path = mesAutoConfig.luceneIndexPath(clazz);
        return DirectoryReader.open(FSDirectory.open(path));
    }
}
