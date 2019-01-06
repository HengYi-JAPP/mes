package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.SilkExceptionUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkException;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-21
 */
public interface SilkExceptionService {

    Single<SilkException> create(Principal principal, SilkExceptionUpdateCommand command);

    Single<SilkException> update(Principal principal, String id, SilkExceptionUpdateCommand command);
}
