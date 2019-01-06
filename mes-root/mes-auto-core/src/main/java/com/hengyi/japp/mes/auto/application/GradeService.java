package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.GradeUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Grade;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface GradeService {

    Single<Grade> create(Principal principal, GradeUpdateCommand command);

    Single<Grade> update(Principal principal, String id, GradeUpdateCommand command);
}
