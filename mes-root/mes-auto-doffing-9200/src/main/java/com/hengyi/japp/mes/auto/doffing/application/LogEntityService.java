package com.hengyi.japp.mes.auto.doffing.application;

import com.hengyi.japp.mes.auto.doffing.application.command.LogCreateCommand;
import io.reactivex.Completable;

/**
 * @author jzb 2019-03-08
 */
public interface LogEntityService {
    Completable create(LogCreateCommand command);
}
