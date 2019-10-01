package test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import lombok.SneakyThrows;

/**
 * @author jzb 2019-09-27
 */
public class DownloadTest {
    public static final String publicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEraVJ8CpkrwTPRCPluUDdwC6b8+m4\n" +
            "dEjwl8s+Sn0GULko+H95fsTREQ1A2soCFHS4wV3/23Nebq9omY3KuK9DKw==\n";
    public static final String secretKey = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgeRyEfU1NSHPTCuC9\n" +
            "rwLZMukaWCH2Fk6q5w+XBYrKtLihRANCAAStpUnwKmSvBM9EI+W5QN3ALpvz6bh0\n" +
            "SPCXyz5KfQZQuSj4f3l+xNERDUDaygIUdLjBXf/bc15ur2iZjcq4r0Mr";
    public static final Vertx vertx = Vertx.vertx();

    @SneakyThrows
    public static void main(String[] args) {
        testSymmetric();
        testEC();
        vertx.deployVerticle(new AbstractVerticle() {
        });
    }

    private static void testEC() {
        JWTAuth provider = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("ES256")
                        .setPublicKey(publicKey)
                        .setSecretKey(secretKey)
                ));
        final JsonObject jsonObject = new JsonObject().put("path", "path").put("fileName", "fileName");
        final String downloadToken = provider.generateToken(jsonObject, new JWTOptions().setAlgorithm("ES256"));

        provider.authenticate(new JsonObject().put("jwt", downloadToken), ar -> {
            final User result = ar.result();
            System.out.println("=========EC=========");
            System.out.println(downloadToken);
            System.out.println(result.principal());
        });
    }

    private static void testSymmetric() {
        JWTAuth provider = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey("japp-mes-auto-download")
                        .setSymmetric(true)));
        final JsonObject jsonObject = new JsonObject().put("path", "path").put("fileName", "fileName");
        final String downloadToken = provider.generateToken(jsonObject);

        provider.authenticate(new JsonObject().put("jwt", downloadToken), ar -> {
            final User result = ar.result();
            System.out.println("=========Symmetric=========");
            System.out.println(downloadToken);
            System.out.println(result.principal());
        });
    }
}
