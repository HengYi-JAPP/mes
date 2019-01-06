package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.SilkCarUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface SilkCarService {

    Single<SilkCar> create(Principal principal, SilkCarUpdateCommand command);

    Single<SilkCar> update(Principal principal, String id, SilkCarUpdateCommand command);
}
