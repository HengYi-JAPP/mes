package file;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.worker.WorkerModule;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import lombok.SneakyThrows;

import static com.hengyi.japp.mes.auto.Constant.JWT_ALGORITHM;
import static com.hengyi.japp.mes.auto.worker.Worker.INJECTOR;

/**
 * @author jzb 2019-03-15
 */
public class WatchTest {
    @SneakyThrows
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new WorkerModule());

        printToken("if_riamb_CD_1");
        printToken("if_riamb_CD_2");
        printToken("if_riamb_CD_3");
        printToken("if_riamb_A_1");
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
