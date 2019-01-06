package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.command.TokenCommand;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.OperatorGroup;
import com.hengyi.japp.mes.auto.domain.Permission;
import com.hengyi.japp.mes.auto.domain.ProductProcess;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.security.Principal;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jzb 2018-06-22
 */
public interface AuthService {

    default String encodeToString(final byte[] bytes) {
        Validate.notNull(bytes);
        final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

    default String encodeToString(final String s) {
        Validate.notBlank(s);
        final byte[] bytes = s.getBytes(UTF_8);
        return encodeToString(bytes);
    }

    default String encodeToString(final JsonObject o) {
        Validate.notNull(o);
        return encodeToString(o.encode());
    }

    default Stream<String> fetchRoles(Operator operator) {
        return Stream.concat(
                CollectionUtils.emptyIfNull(operator.getRoles()).stream(),
                CollectionUtils.emptyIfNull(operator.getGroups()).stream()
                        .map(OperatorGroup::getRoles)
                        .filter(J::nonEmpty)
                        .flatMap(Collection::stream)
        ).map(RoleType::name).distinct();
    }

    default Stream<String> fetchPermission(Operator operator) {
        return Stream.concat(
                CollectionUtils.emptyIfNull(operator.getPermissions()).stream(),
                CollectionUtils.emptyIfNull(operator.getGroups()).stream()
                        .map(OperatorGroup::getPermissions)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
        ).map(Permission::getCode).distinct();
    }

    Single<String> token(TokenCommand command);

    Single<String> authInfo(Principal principal);

    Completable checkRole(Operator operator, RoleType roleType);

    Completable checkRole(Principal principal, RoleType roleType);

    Completable checkPermission(Operator operator, String code);

    Completable checkPermission(Principal principal, String code);

    default Completable checkProductProcessSubmit(Operator operator, String productProcessId) {
        return checkPermission(operator, "ProductProcess:" + productProcessId);
    }

    default Completable checkProductProcessSubmit(Principal principal, String productProcessId) {
        return checkPermission(principal, "ProductProcess:" + productProcessId);
    }

    default Completable checkProductProcessSubmit(Operator operator, ProductProcess productProcess) {
        return checkProductProcessSubmit(operator, productProcess.getId());
    }

    void signEventHandleResult(RoutingContext rc, String body);
}
