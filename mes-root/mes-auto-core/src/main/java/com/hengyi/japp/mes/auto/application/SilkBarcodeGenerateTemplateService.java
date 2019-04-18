package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.SilkBarcodeGenerateTemplateUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkBarcodeGenerateTemplate;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface SilkBarcodeGenerateTemplateService {
    Single<SilkBarcodeGenerateTemplate> create(Principal principal, SilkBarcodeGenerateTemplateUpdateCommand command);
}
