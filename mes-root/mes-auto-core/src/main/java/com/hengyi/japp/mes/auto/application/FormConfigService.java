package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.FormConfigUpdateCommand;
import com.hengyi.japp.mes.auto.domain.FormConfig;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface FormConfigService {

    Single<FormConfig> create(Principal principal, FormConfigUpdateCommand command);

    Single<FormConfig> update(Principal principal, String id, FormConfigUpdateCommand command);
}
