package com.hengyi.japp.mes.auto.report;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import lombok.extern.slf4j.Slf4j;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class ReportTest {

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                final Router router = Router.router(vertx);
                router.route().handler(BodyHandler.create());
                router.route().handler(ResponseContentTypeHandler.create());
                Jvertx.enableCors(io.vertx.reactivex.ext.web.Router.newInstance(router), Sets.newHashSet());

                router.route().handler(rc -> {
                    rc.response().putHeader("content-type", APPLICATION_JSON)
                            .end("[{\"operator\":{\"id\":\"5c77815e26e0ff0001961888\",\"deleted\":false,\"name\":\"张红梅\",\"hrId\":\"18008573\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":50,\"silkCount\":50}]},{\"operator\":{\"id\":\"5c77794826e0ff0001790744\",\"deleted\":false,\"name\":\"蒋吉平\",\"hrId\":\"18011421\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":216,\"silkCount\":216}]},{\"operator\":{\"id\":\"5c777f3c26e0ff000190773d\",\"deleted\":false,\"name\":\"王春菊\",\"hrId\":\"18009719\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":256,\"silkCount\":256}]},{\"operator\":{\"id\":\"5d3a478f105bc60001575331\",\"deleted\":false,\"name\":\"刘翠娟\",\"hrId\":\"29002190\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":53,\"silkCount\":53}]},{\"operator\":{\"id\":\"5cb58c7c1c0cbf00019a4054\",\"deleted\":false,\"name\":\"谭化红\",\"hrId\":\"18011737\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":3,\"silkCount\":3}]},{\"operator\":{\"id\":\"5c777d4626e0ff000189e77b\",\"deleted\":false,\"name\":\"陈坤\",\"hrId\":\"18005271\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":2,\"silkCount\":2}]},{\"operator\":{\"id\":\"5c7781b726e0ff00019706b6\",\"deleted\":false,\"name\":\"王琴娣\",\"hrId\":\"18008612\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":247,\"silkCount\":247}]},{\"operator\":{\"id\":\"5c6cdcdd3d0045000125b2d0\",\"deleted\":false,\"name\":\"柳银红\",\"hrId\":\"18009108\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":232,\"silkCount\":232}]},{\"operator\":{\"id\":\"5c6cd9243d0045000120cad6\",\"deleted\":false,\"name\":\"王景申\",\"hrId\":\"18004013\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":55,\"silkCount\":55}]},{\"operator\":{\"id\":\"5c777b8226e0ff0001805ffe\",\"deleted\":false,\"name\":\"诸菊花\",\"hrId\":\"18004423\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":94,\"silkCount\":94}]},{\"operator\":{\"id\":\"5c777c1926e0ff0001840367\",\"deleted\":false,\"name\":\"雍朝辉\",\"hrId\":\"18004578\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":273,\"silkCount\":273}]},{\"operator\":{\"id\":\"anonymous\",\"deleted\":false,\"name\":null,\"hrId\":null,\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":279,\"silkCount\":279}]},{\"operator\":{\"id\":\"5c77810c26e0ff0001952f0a\",\"deleted\":false,\"name\":\"陈小茹\",\"hrId\":\"18008395\",\"oaId\":null,\"phone\":null,\"admin\":false},\"groupByProducts\":[{\"product\":{\"id\":\"5bffa63c8857b85a437d1f93\",\"deleted\":false,\"createDateTime\":null,\"modifyDateTime\":null,\"name\":\"FDY\",\"code\":null},\"silkCarRecordCount\":12,\"silkCount\":12}]}]");
                });
                vertx.createHttpServer().requestHandler(router).listen(9998);
            }
        });
        System.out.println("test stated");
    }

}
