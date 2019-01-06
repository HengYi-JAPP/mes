package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.PermissionUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Permission;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.PermissionRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class PermissionServiceImpl implements PermissionService {
    private final OperatorRepository operatorRepository;
    private final PermissionRepository permissionRepository;

    @Inject
    private PermissionServiceImpl(OperatorRepository operatorRepository, PermissionRepository permissionRepository) {
        this.operatorRepository = operatorRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Single<Permission> create(Principal principal, PermissionUpdateCommand command) {
        return permissionRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Permission> save(Principal principal, Permission permission, PermissionUpdateCommand command) {
        permission.setName(command.getName());
        permission.setCode(command.getCode());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    permission.log(operator);
                    return permissionRepository.save(permission);
                });
    }

    @Override
    public Single<Permission> update(Principal principal, String id, PermissionUpdateCommand command) {
        return permissionRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
