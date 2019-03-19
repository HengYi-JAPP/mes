package file;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.interfaces.ruiguan.RuiguanService;
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
        final String body = "{\"id\":\"C420190319095453YJ036P0866\",\"line\":\"C4\",\"principalName\":\"if_ruiguan\",\"createDateTime\":1552960493000,\"silkCarInfo\":{\"code\":\"YJ036P0866\",\"row\":3,\"col\":6,\"batchNo\":\"HCO53001\",\"batchSpec\":\"530/96+ \",\"grade\":\"AA\"},\"silkInfos\":[{\"sideType\":\"A\",\"row\":1,\"col\":1,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":1,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"A\",\"row\":1,\"col\":2,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":2,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"A\",\"row\":1,\"col\":3,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":3,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"A\",\"row\":1,\"col\":4,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":4,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"A\",\"row\":1,\"col\":5,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":5,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"A\",\"row\":1,\"col\":6,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":6,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"A\",\"row\":2,\"col\":1,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":1,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"A\",\"row\":2,\"col\":2,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":2,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"A\",\"row\":2,\"col\":3,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":3,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"A\",\"row\":2,\"col\":4,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":4,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"A\",\"row\":2,\"col\":5,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":5,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"A\",\"row\":2,\"col\":6,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":6,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"A\",\"row\":3,\"col\":1,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":1,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"A\",\"row\":3,\"col\":2,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":2,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"A\",\"row\":3,\"col\":3,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":3,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"A\",\"row\":3,\"col\":4,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":4,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"A\",\"row\":3,\"col\":5,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":5,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"A\",\"row\":3,\"col\":6,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":6,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"B\",\"row\":1,\"col\":1,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":7,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"B\",\"row\":1,\"col\":2,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":8,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"B\",\"row\":1,\"col\":3,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":9,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"B\",\"row\":1,\"col\":4,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":10,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"B\",\"row\":1,\"col\":5,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":11,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"B\",\"row\":1,\"col\":6,\"line\":\"C4\",\"lineMachine\":11,\"spindle\":12,\"timestamp\":1552959408,\"doffingDateTime\":1552959408000},{\"sideType\":\"B\",\"row\":2,\"col\":1,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":7,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"B\",\"row\":2,\"col\":2,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":8,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"B\",\"row\":2,\"col\":3,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":9,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"B\",\"row\":2,\"col\":4,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":10,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"B\",\"row\":2,\"col\":5,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":11,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"B\",\"row\":2,\"col\":6,\"line\":\"C4\",\"lineMachine\":15,\"spindle\":12,\"timestamp\":1552959605,\"doffingDateTime\":1552959605000},{\"sideType\":\"B\",\"row\":3,\"col\":1,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":7,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"B\",\"row\":3,\"col\":2,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":8,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"B\",\"row\":3,\"col\":3,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":9,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"B\",\"row\":3,\"col\":4,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":10,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"B\",\"row\":3,\"col\":5,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":11,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000},{\"sideType\":\"B\",\"row\":3,\"col\":6,\"line\":\"C4\",\"lineMachine\":5,\"spindle\":12,\"timestamp\":1552959703,\"doffingDateTime\":1552959703000}]}";
        final var command = MAPPER.readValue(body, SilkCarRuntimeInitEvent.RuiguanAutoDoffingCommand.class);
        ruiguanService.handle(RuiguanService.PRINCIPAL, command).subscribe();

//        final SilkCarRecordRepository silkCarRecordRepository = INJECTOR.getInstance(SilkCarRecordRepository.class);
//        silkCarRecordRepository.find("C620190318210408YJ036P0076")
//                .flatMapCompletable(ruiguanService::printSilk)
//                .subscribe();

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
