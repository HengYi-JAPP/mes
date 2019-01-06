package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.LineUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Line;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface LineService {

    Single<Line> create(Principal principal, LineUpdateCommand command);

    Single<Line> update(Principal principal, String id, LineUpdateCommand command);

    Completable delete(Principal principal, String id);

}
