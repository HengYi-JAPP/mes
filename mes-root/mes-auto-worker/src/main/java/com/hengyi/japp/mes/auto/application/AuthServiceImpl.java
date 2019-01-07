package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.codec.Jcodec;
import com.github.ixtf.japp.core.exception.JAuthorizationException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.TokenCommand;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.repository.LoginRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;

import java.security.*;
import java.util.concurrent.TimeUnit;

import static com.hengyi.japp.mes.auto.Constant.IF_SIGN_HEADER;
import static com.hengyi.japp.mes.auto.Constant.JWT_ALGORITHM;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class AuthServiceImpl implements AuthService {
    private final JWTAuth jwtAuth;
    private final PrivateKey privateKey;
    private final OperatorRepository operatorRepository;
    private final LoginRepository loginRepository;

    @Inject
    private AuthServiceImpl(JWTAuth jwtAuth, PrivateKey privateKey, OperatorRepository operatorRepository, LoginRepository loginRepository) {
        this.jwtAuth = jwtAuth;
        this.privateKey = privateKey;
        this.operatorRepository = operatorRepository;
        this.loginRepository = loginRepository;
    }

    @Override
    public Single<String> token(TokenCommand command) {
        return loginRepository.findByLoginId(command.getLoginId()).map(login -> {
            if (Jcodec.checkPassword(login.getPassword(), command.getLoginPassword())) {
                return login.getOperator();
            }
            throw new RuntimeException();
        }).map(operator -> {
            final JWTOptions options = new JWTOptions()
                    .setSubject(operator.getId())
                    .setAlgorithm(JWT_ALGORITHM)
                    .setIssuer("japp-mes-auto")
                    .setExpiresInMinutes((int) TimeUnit.HOURS.toMinutes(12));
            final JsonObject claims = new JsonObject().put("uid", operator.getId());
            return jwtAuth.generateToken(claims, options);
        }).toSingle();
    }

    @Override
    public Single<String> authInfo(Principal principal) {
        return operatorRepository.find(principal).map(operator -> {
            final JsonObject result = new JsonObject();
            final JsonArray roles = new JsonArray();
            final JsonArray permissions = new JsonArray();
            result.put("id", operator.getId())
                    .put("name", operator.getName())
                    .put("admin", operator.isAdmin())
                    .put("hrId", operator.getHrId())
                    .put("oaId", operator.getOaId())
                    .put("roles", roles)
                    .put("permissions", permissions);
            fetchRoles(operator).forEach(roles::add);
            fetchPermission(operator).forEach(permissions::add);
            return result.encode();
        });
    }

    @Override
    public Completable checkRole(Operator operator, RoleType roleType) {
        if (operator.isAdmin()) {
            return Completable.complete();
        }
        return fetchRoles(operator)
                .filter(it -> it.contains(roleType.name()))
                .findFirst()
                .map(it -> Completable.complete())
                .orElse(Completable.error(new JAuthorizationException()));
    }

    @Override
    public Completable checkRole(Principal principal, RoleType roleType) {
        return operatorRepository.find(principal).flatMapCompletable(operator -> checkRole(operator, roleType));
    }

    @Override
    public Completable checkPermission(Operator operator, String code) {
        if (operator.isAdmin()) {
            return Completable.complete();
        }
        return fetchPermission(operator)
                .filter(it -> it.equals(code))
                .findFirst()
                .map(it -> Completable.complete())
                .orElse(Completable.error(new JAuthorizationException()));
    }

    @Override
    public Completable checkPermission(Principal principal, String code) {
        return operatorRepository.find(principal).flatMapCompletable(operator -> checkPermission(operator, code));
    }

    @Override
    public void signEventHandleResult(RoutingContext rc, final String body) {
        Single.fromCallable(() -> {
            final JsonObject header = new JsonObject()
                    .put("typ", "JWT")
                    .put("alg", "RS512");
            final String headerSeg = encodeToString(header);

            final JsonObject payload = new JsonObject()
                    .put("iss", "japp-mes")
                    .put("timestamp", System.currentTimeMillis())
                    .put("content-sha1", DigestUtils.sha1Hex(encodeToString(body)));
            final String payloadSeg = encodeToString(payload);

            return headerSeg + "." + payloadSeg + "." + sign(headerSeg + "." + payloadSeg);
        }).subscribe(sign -> rc.response().putHeader(IF_SIGN_HEADER, sign).end(body));
    }

    private String sign(final String s) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Validate.notBlank(s);
        final Signature sig = Signature.getInstance("SHA512withRSA");
        sig.initSign(privateKey);
        sig.update(s.getBytes(UTF_8));
        return encodeToString(sig.sign());
    }
}
