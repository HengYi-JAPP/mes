package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.FormConfigUpdateCommand;
import com.hengyi.japp.mes.auto.domain.FormConfig;
import com.hengyi.japp.mes.auto.repository.FormConfigRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class FormConfigServiceImpl implements FormConfigService {
    private final FormConfigRepository formConfigRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private FormConfigServiceImpl(FormConfigRepository formConfigRepository, OperatorRepository operatorRepository) {
        this.formConfigRepository = formConfigRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<FormConfig> create(Principal principal, FormConfigUpdateCommand command) {
        return formConfigRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<FormConfig> save(Principal principal, FormConfig formConfig, FormConfigUpdateCommand command) {
        formConfig.setName(command.getName());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    formConfig.formFieldsConfig(command.getFormFieldConfigs());
                    formConfig.log(operator);
                    return formConfigRepository.save(formConfig);
                });
    }

    @Override
    public Single<FormConfig> update(Principal principal, String id, FormConfigUpdateCommand command) {
        return formConfigRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
