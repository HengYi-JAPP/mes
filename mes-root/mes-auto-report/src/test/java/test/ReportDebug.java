package test;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.report.ReportModule;
import com.hengyi.japp.mes.auto.report.ReportVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class ReportDebug {
    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true).setMaxWorkerExecuteTime(Duration.ofDays(1).toNanos());
        vertx.rxDeployVerticle(ReportVerticle.class.getName(), deploymentOptions).subscribe();
    }

}
