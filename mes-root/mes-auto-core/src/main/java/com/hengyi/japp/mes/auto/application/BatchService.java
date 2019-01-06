package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.BatchUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Batch;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface BatchService {
    Single<Batch> create(Principal principal, BatchUpdateCommand command);

    Single<Batch> update(Principal principal, String id, BatchUpdateCommand command);
}
