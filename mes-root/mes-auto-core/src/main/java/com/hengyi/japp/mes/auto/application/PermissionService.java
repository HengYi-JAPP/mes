package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.PermissionUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Permission;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface PermissionService {

    Single<Permission> create(Principal principal, PermissionUpdateCommand command);

    Single<Permission> update(Principal principal, String id, PermissionUpdateCommand command);

}
