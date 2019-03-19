package file;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.interfaces.ruiguan.RuiguanService;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordRepository;
import com.hengyi.japp.mes.auto.worker.WorkerModule;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.Constant.JWT_ALGORITHM;
import static com.hengyi.japp.mes.auto.worker.Worker.INJECTOR;

/**
 * @author jzb 2019-03-15
 */
public class WatchTest {
    @SneakyThrows
    public static void main(String[] args) {
        final VertxOptions vertxOptions = new VertxOptions().setMaxEventLoopExecuteTime(TimeUnit.DAYS.toNanos(1));
        final Vertx vertx = Vertx.vertx(vertxOptions);
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new WorkerModule());

        final RuiguanService ruiguanService = INJECTOR.getInstance(RuiguanService.class);
        final String body = "{\"id\":\"C620190318212400YJ036P0424\",\"line\":\"C6\",\"principalName\":\"if_ruiguan\",\"createDateTime\":1552915440000,\"silkCarInfo\":{\"code\":\"YJ036P0424\",\"row\":3,\"col\":6,\"batchNo\":\"HC052002\",\"batchSpec\":\"368/288 \",\"grade\":\"AA\"},\"silkInfos\":[{\"sideType\":\"A\",\"row\":1,\"col\":1,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":1,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"A\",\"row\":1,\"col\":2,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":2,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"A\",\"row\":1,\"col\":3,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":3,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"A\",\"row\":1,\"col\":4,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":4,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"A\",\"row\":1,\"col\":5,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":5,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"A\",\"row\":1,\"col\":6,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":6,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"A\",\"row\":2,\"col\":1,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":1,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"A\",\"row\":2,\"col\":2,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":2,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"A\",\"row\":2,\"col\":3,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":3,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"A\",\"row\":2,\"col\":4,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":4,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"A\",\"row\":2,\"col\":5,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":5,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"A\",\"row\":2,\"col\":6,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":6,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"A\",\"row\":3,\"col\":1,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":1,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"A\",\"row\":3,\"col\":2,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":2,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"A\",\"row\":3,\"col\":3,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":3,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"A\",\"row\":3,\"col\":4,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":4,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"A\",\"row\":3,\"col\":5,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":5,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"A\",\"row\":3,\"col\":6,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":6,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"B\",\"row\":1,\"col\":1,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":7,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"B\",\"row\":1,\"col\":2,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":8,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"B\",\"row\":1,\"col\":3,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":9,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"B\",\"row\":1,\"col\":4,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":10,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"B\",\"row\":1,\"col\":5,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":11,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"B\",\"row\":1,\"col\":6,\"line\":\"C6\",\"lineMachine\":28,\"spindle\":12,\"timestamp\":1552912817,\"doffingDateTime\":1552912817000},{\"sideType\":\"B\",\"row\":2,\"col\":1,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":7,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"B\",\"row\":2,\"col\":2,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":8,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"B\",\"row\":2,\"col\":3,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":9,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"B\",\"row\":2,\"col\":4,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":10,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"B\",\"row\":2,\"col\":5,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":11,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"B\",\"row\":2,\"col\":6,\"line\":\"C6\",\"lineMachine\":48,\"spindle\":12,\"timestamp\":1552914804,\"doffingDateTime\":1552914804000},{\"sideType\":\"B\",\"row\":3,\"col\":1,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":7,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"B\",\"row\":3,\"col\":2,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":8,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"B\",\"row\":3,\"col\":3,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":9,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"B\",\"row\":3,\"col\":4,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":10,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"B\",\"row\":3,\"col\":5,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":11,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000},{\"sideType\":\"B\",\"row\":3,\"col\":6,\"line\":\"C6\",\"lineMachine\":26,\"spindle\":12,\"timestamp\":1552914760,\"doffingDateTime\":1552914760000}]}";
        final var command = MAPPER.readValue(body, SilkCarRuntimeInitEvent.RuiguanAutoDoffingCommand.class);
        ruiguanService.handle(RuiguanService.PRINCIPAL, command).subscribe();

        final SilkCarRecordRepository silkCarRecordRepository = INJECTOR.getInstance(SilkCarRecordRepository.class);
        silkCarRecordRepository.find("C620190318210408YJ036P0076")
                .flatMapCompletable(ruiguanService::printSilk)
                .subscribe();

        TimeUnit.DAYS.sleep(1);
//        printToken("if_riamb_CD_1");
//        printToken("if_riamb_CD_2");
//        printToken("if_riamb_CD_3");
//        printToken("if_riamb_A_1");
    }

    private static void printToken(String id) {
        final JWTAuth jwtAuth = INJECTOR.getInstance(JWTAuth.class);
        final JWTOptions options = new JWTOptions()
                .setSubject(id)
                .setAlgorithm(JWT_ALGORITHM)
                .setIssuer("japp-mes-auto");
        final JsonObject claims = new JsonObject().put("uid", id);
        final String s = jwtAuth.generateToken(claims, options);
        System.out.println(id);
        System.out.println(s);
    }
}
