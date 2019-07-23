package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.ExceptionRecordUpdateCommand;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface ExceptionRecordService {

    Single<ExceptionRecord> create(Principal principal, ExceptionRecordUpdateCommand command);

    Single<ExceptionRecord> update(Principal principal, String id, ExceptionRecordUpdateCommand command);

    Completable handle(Principal principal, String id);
}
