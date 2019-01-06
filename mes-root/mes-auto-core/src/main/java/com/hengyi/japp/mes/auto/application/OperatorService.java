package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.OperatorCreateCommand;
import com.hengyi.japp.mes.auto.application.command.OperatorImportCommand;
import com.hengyi.japp.mes.auto.application.command.OperatorPermissionUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.PasswordChangeCommand;
import com.hengyi.japp.mes.auto.domain.Operator;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface OperatorService {

    Single<Operator> create(Principal principal, OperatorImportCommand command);

    Single<Operator> create(Principal principal, OperatorCreateCommand command);

    Single<Operator> update(Principal principal, String id, OperatorPermissionUpdateCommand command);

    Completable password(String id, PasswordChangeCommand command);
}
