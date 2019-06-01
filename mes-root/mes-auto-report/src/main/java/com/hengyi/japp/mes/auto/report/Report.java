package com.hengyi.japp.mes.auto.report;

import com.google.inject.Injector;

/**
 * @author jzb 2019-02-23
 */
public class Report {
    public static Injector INJECTOR;

//    public static void main(String[] args) {
//        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
//            INJECTOR = Guice.createInjector(new GuiceModule(vertx), new WorkerModule());
//
//            return deployWorker(vertx).ignoreElement();
//        }).subscribe();
//
//        @SneakyThrows
//        private static VertxOptions vertxOptions () {
//            final VertxOptions vertxOptions = new VertxOptions()
//                    .setWorkerPoolSize(1000)
//                    .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(1000000))
//                    .setMaxWorkerExecuteTime(TimeUnit.MINUTES.toNanos(5));
//            Optional.ofNullable(System.getProperty("vertx.cluster.host"))
//                    .filter(J::nonBlank)
//                    .ifPresent(vertxOptions::setClusterHost);
//            return vertxOptions;
//        }
//    }

}
