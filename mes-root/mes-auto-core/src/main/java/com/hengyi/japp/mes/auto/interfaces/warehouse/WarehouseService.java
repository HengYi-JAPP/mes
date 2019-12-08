package com.hengyi.japp.mes.auto.interfaces.warehouse;

import com.hengyi.japp.mes.auto.application.event.PackageBoxEvent;
import com.hengyi.japp.mes.auto.application.event.PackageBoxFlipEvent;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.PackageBoxFlip;
import com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent;
import com.sun.security.auth.UserPrincipal;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * 仓库接口
 *
 * @author jzb 2018-06-25
 */
public interface WarehouseService {
    Principal PRINCIPAL = new UserPrincipal("if_warehouse");

    Single<PackageBox> handle(Principal principal, WarehousePackageBoxFetchEvent.Command command);

    Completable unFetch(Principal principal, String code);

    Single<PackageBoxFlip> handle(Principal principal, PackageBoxFlipEvent.WarehouseCommand command);

    Single<PackageBox> handle(Principal principal, PackageBoxEvent.BigSilkCarCommand command);

    Single<PackageBox> handle(Principal principal, WarehouseMeasureInfoUpdateCommand command);
}
