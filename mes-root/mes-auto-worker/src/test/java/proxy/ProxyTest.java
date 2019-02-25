package proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.worker.WorkerModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;

/**
 * @author jzb 2019-02-24
 */
public class ProxyTest {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new WorkerModule());

        vertx.deployVerticle(new AbstractVerticle() {
        });

        final MongoClient mongoClient = INJECTOR.getInstance(MongoClient.class);
        final String s = "{\"creator\":\"5c04a044c3cae813b530cdd1\",\"lineMachine\":\"5bfd4b87716bb151bd059df3\",\"mdt\":{\"$date\":\"2019-02-24T09:54:16+08:00\"},\"cdt\":{\"$date\":\"2019-02-24T09:54:16+08:00\"},\"batch\":\"5bfd4b87716bb151bd059db5\",\"codeDate\":{\"$date\":\"2019-02-24T00:00:00+08:00\"},\"code\":\"005R0000T\",\"modifier\":\"5c04a044c3cae813b530cdd1\",\"doffingNum\":\"A1\",\"codeDoffingNum\":29,\"_id\":\"5c71f9451e332602e39b4441\"}";
        final JsonObject document = new JsonObject(s);
//        final String id = mongoClient.rxSave("T_SilkBarcode", document).blockingGet();
        mongoClient.rxSave("T_SilkBarcode", document).defaultIfEmpty("test")
                .subscribe(it -> {
                    System.out.println(it);
                }, ex -> ex.printStackTrace());


    }
}
