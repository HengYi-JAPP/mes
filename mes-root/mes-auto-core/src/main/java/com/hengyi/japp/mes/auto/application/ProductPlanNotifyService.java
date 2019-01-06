package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.ProductPlanNotifyExeCommand;
import com.hengyi.japp.mes.auto.application.command.ProductPlanNotifyUpdateCommand;
import com.hengyi.japp.mes.auto.domain.ProductPlanNotify;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-21
 */
public interface ProductPlanNotifyService {

    Single<ProductPlanNotify> create(Principal principal, ProductPlanNotifyUpdateCommand command);

    Single<ProductPlanNotify> update(Principal principal, String id, ProductPlanNotifyUpdateCommand command);

    Completable exe(Principal principal, String id, ProductPlanNotifyExeCommand command);

    Completable unExe(Principal principal, String id, ProductPlanNotifyExeCommand command);

    Completable finish(Principal principal, String id);

    Completable unFinish(Principal principal, String id);
}
