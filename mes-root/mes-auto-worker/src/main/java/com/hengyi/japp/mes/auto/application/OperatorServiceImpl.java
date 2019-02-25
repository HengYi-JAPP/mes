package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.codec.Jcodec;
import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.OperatorCreateCommand;
import com.hengyi.japp.mes.auto.application.command.OperatorImportCommand;
import com.hengyi.japp.mes.auto.application.command.OperatorPermissionUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.PasswordChangeCommand;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.OperatorGroup;
import com.hengyi.japp.mes.auto.domain.Permission;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.LoginRepository;
import com.hengyi.japp.mes.auto.repository.OperatorGroupRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.PermissionRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.security.Principal;
import java.util.List;


/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class OperatorServiceImpl implements OperatorService {
    private final OperatorRepository operatorRepository;
    private final OperatorGroupRepository operatorGroupRepository;
    private final PermissionRepository permissionRepository;
    private final LoginRepository loginRepository;

    @Inject
    private OperatorServiceImpl(OperatorRepository operatorRepository, OperatorGroupRepository operatorGroupRepository, PermissionRepository permissionRepository, LoginRepository loginRepository) {
        this.operatorRepository = operatorRepository;
        this.operatorGroupRepository = operatorGroupRepository;
        this.permissionRepository = permissionRepository;
        this.loginRepository = loginRepository;
    }

    @Override
    synchronized public Single<Operator> create(Principal principal, OperatorImportCommand command) {
        final String hrId = command.getHrId();
        final Maybe<Operator> byHrId$ = J.nonBlank(hrId) ? operatorRepository.findByHrId(hrId) : Maybe.empty();
        final String oaId = command.getOaId();
        final Maybe<Operator> byOaId$ = J.nonBlank(oaId) ? operatorRepository.findByOaId(oaId) : Maybe.empty();
        final Single<Operator> create$ = operatorRepository.create().flatMap(operator -> {
            operator.setHrId(command.getHrId());
            operator.setOaId(command.getOaId());
            operator.setName(command.getName());
            return operatorRepository.save(operator);
        });
        return byHrId$.switchIfEmpty(byOaId$).switchIfEmpty(create$);
    }

    @Override
    public Single<Operator> create(Principal principal, OperatorCreateCommand command) {
        final String hrId = command.getHrId();
        final Maybe<Operator> byHrId$ = J.nonBlank(hrId) ? operatorRepository.findByHrId(hrId) : Maybe.empty();
        final String oaId = command.getOaId();
        final Maybe<Operator> byOaId$ = J.nonBlank(oaId) ? operatorRepository.findByOaId(oaId) : Maybe.empty();
        final Single<Operator> create$ = operatorRepository.create().flatMap(operator -> {
            operator.setHrId(command.getHrId());
            operator.setOaId(command.getOaId());
            operator.setName(command.getName());
            return operatorRepository.save(operator);
        }).flatMap(operator -> loginRepository.create().flatMap(login -> {
            login.setPassword(Jcodec.password(command.getPassword()));
            login.setOperator(operator);
            login.setLoginId(operator.getHrId());
            return loginRepository.save(login).map(it -> operator);
        }));
        return byHrId$.switchIfEmpty(byOaId$).switchIfEmpty(create$);
    }

    @Override
    public Single<Operator> update(Principal principal, String id, OperatorPermissionUpdateCommand command) {
        final Single<List<OperatorGroup>> groups$ = Flowable.fromIterable(CollectionUtils.emptyIfNull(command.getGroups()))
                .map(EntityDTO::getId)
                .distinct()
                .flatMapSingle(operatorGroupRepository::find)
                .toList();
        final Single<List<Permission>> permissions$ = Flowable.fromIterable(CollectionUtils.emptyIfNull(command.getPermissions()))
                .map(EntityDTO::getId)
                .distinct()
                .flatMapSingle(permissionRepository::find)
                .toList();
        return operatorRepository.find(id)
                .flatMap(operator -> {
                    operator.setAdmin(command.isAdmin());
                    operator.setRoles(command.getRoles());
                    return groups$.flatMap(groups -> {
                        operator.setGroups(groups);
                        return permissions$;
                    }).map(permissions -> {
                        operator.setPermissions(permissions);
                        return operator;
                    });
                })
                .flatMap(operatorRepository::save);
    }

    @Override
    public Completable password(String id, PasswordChangeCommand command) {
        return loginRepository.find(id).flatMap(login -> {
            final String password = command.getNewPassword();
            if (password.equals(command.getNewPasswordAgain())) {
                if (Jcodec.checkPassword(login.getPassword(), command.getOldPassword())) {
                    login.setPassword(Jcodec.password(password));
                    return loginRepository.save(login);
                }
            }
            throw new RuntimeException();
        }).ignoreElement();
    }

}
