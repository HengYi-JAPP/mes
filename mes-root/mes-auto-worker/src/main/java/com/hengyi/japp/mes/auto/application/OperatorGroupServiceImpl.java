package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.OperatorGroupUpdateCommand;
import com.hengyi.japp.mes.auto.domain.OperatorGroup;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.OperatorGroupRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.PermissionRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class OperatorGroupServiceImpl implements OperatorGroupService {
    private final OperatorRepository operatorRepository;
    private final OperatorGroupRepository operatorGroupRepository;
    private final PermissionRepository permissionRepository;

    @Inject
    private OperatorGroupServiceImpl(OperatorRepository operatorRepository, OperatorGroupRepository operatorGroupRepository, PermissionRepository permissionRepository) {
        this.operatorRepository = operatorRepository;
        this.operatorGroupRepository = operatorGroupRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Single<OperatorGroup> create(Principal principal, OperatorGroupUpdateCommand command) {
        return operatorGroupRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<OperatorGroup> save(Principal principal, OperatorGroup operatorGroup, OperatorGroupUpdateCommand command) {
        operatorGroup.setName(command.getName());
        operatorGroup.setRoles(command.getRoles());
        return Flowable.fromIterable(CollectionUtils.emptyIfNull(command.getPermissions()))
                .map(EntityDTO::getId).distinct()
                .flatMapSingle(permissionRepository::find).toList()
                .flatMap(permissions -> {
                    operatorGroup.setPermissions(permissions);
                    return operatorRepository.find(principal);
                })
                .flatMap(operator -> {
                    operatorGroup.log(operator);
                    return operatorGroupRepository.save(operatorGroup);
                });
    }

    @Override
    public Single<OperatorGroup> update(Principal principal, String id, OperatorGroupUpdateCommand command) {
        return operatorGroupRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
