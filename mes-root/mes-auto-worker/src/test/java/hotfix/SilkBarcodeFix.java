package hotfix;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.japp.core.J;
import com.google.common.base.Strings;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import io.vertx.core.json.JsonObject;
import lombok.Cleanup;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static init.UserImport.JSON;
import static init.UserImport.client;

/**
 * @author jzb 2018-11-30
 */
public class SilkBarcodeFix {
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDY4NDg3MTksImV4cCI6MTU0Njg3NzUxOSwiaXNzIjoiamFwcC1tZXMtYXV0byIsInN1YiI6IjVjMDRhMDQ0YzNjYWU4MTNiNTMwY2RkMSJ9.OKGXT_8KrOoTwAEogKagYvyBcUJ6f8JkDOl9iTUBKX25NXtphteb4xxuGDjaGT6HVwvcWtHiivf54RcX8up3i30cgW_7K4DQCs-ruTtR7sagtlMggzfimdSsaqUjMafsQ-Hm0rpr0NmocjCfBSg_ZODd-FipUTzybWvFKfww4a86mVT6t3AbmdTYJXXrxZSYgk2DOpp5n3SgdlGT-gaLA5zc5Nxl-ppti2nbCFL7fq4aYNhxsh79BZV9cDkW192FJonEi4gWODy93uUNlPwlw3m5VLKPVnRPVsbwAEdP3wNbxQRO3CpTaK3b8ZvxaP70fpVRoXw_ASxNWiUHhFk9Rw";

    public static void main(String[] args) {
        final String prefix = "010220190107GB0807001B1";
        IntStream.rangeClosed(94, 113)
                .mapToObj(it -> prefix + Strings.padStart("" + it, 3, '0'))
                .map(SilkBarcodeFix::findByCode)
                .forEach(it -> {
                    final PackageBox packageBox = MAPPER.convertValue(it, PackageBox.class);
                    final LocalDate budatLd = J.localDate(packageBox.getBudat());
                    System.out.println(packageBox.getCode() + "\t" + packageBox.getType() + "\t" + budatLd + "\t" + packageBox.getBudatClass().getName() + "\t" + packageBox.getBatch().getBatchNo() + "\t" + packageBox.getSaleType());
                    fix(packageBox.getCode());
                });
        System.out.println("===end===");
    }

    @SneakyThrows
    private static ObjectNode findByCode(String code) {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url("http://10.2.0.215:9998/share/packageBoxes/codes/" + code)
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        return (ObjectNode) MAPPER.readTree(is);
    }

    @SneakyThrows
    private static boolean fix(String code) {
        final RequestBody body = RequestBody.create(JSON, new JsonObject().put("code", code).encode());
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url("http://10.2.0.215:9998/warehouse/PackageBoxFetchEvent")
                .post(body)
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        final boolean result = response.isSuccessful();
        if (!result) {
            @Cleanup final ResponseBody responseBody = response.body();
            System.out.println(responseBody.string());
        }
        return result;
    }
}
