package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SilkBarcodeGenerateTemplateUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkBarcodeGenerateTemplate;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeGenerateTemplateRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkBarcodeGenerateTemplateServiceImpl implements SilkBarcodeGenerateTemplateService {
    private final SilkBarcodeGenerateTemplateRepository silkBarcodeGenerateTemplateRepository;
    private final LineMachineRepository lineMachineRepository;

    @Inject
    private SilkBarcodeGenerateTemplateServiceImpl(SilkBarcodeGenerateTemplateRepository silkBarcodeGenerateTemplateRepository, LineMachineRepository lineMachineRepository) {
        this.silkBarcodeGenerateTemplateRepository = silkBarcodeGenerateTemplateRepository;
        this.lineMachineRepository = lineMachineRepository;
    }

    @Override
    public Single<SilkBarcodeGenerateTemplate> create(Principal principal, SilkBarcodeGenerateTemplateUpdateCommand command) {
        return silkBarcodeGenerateTemplateRepository.create().flatMap(template -> {
            template.setDoffingCount(command.getDoffingCount());
            return lineMachineRepository.find(command.getLineMachine().getId()).flatMap(lineMachine -> {
                template.setLineMachine(lineMachine);
                return silkBarcodeGenerateTemplateRepository.save(template);
            });
        });
    }
}
