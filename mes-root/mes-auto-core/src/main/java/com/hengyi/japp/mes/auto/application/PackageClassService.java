package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.PackageClassUpdateCommand;
import com.hengyi.japp.mes.auto.domain.PackageClass;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface PackageClassService {

    Single<PackageClass> create(Principal principal, PackageClassUpdateCommand command);

    Single<PackageClass> update(Principal principal, String id, PackageClassUpdateCommand command);
}
