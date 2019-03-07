package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.command.PackageBoxAppendCommand;
import com.hengyi.japp.mes.auto.application.command.PackageBoxBatchPrintUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.PackageBoxMeasureInfoUpdateCommand;
import com.hengyi.japp.mes.auto.application.event.PackageBoxEvent;
import com.hengyi.japp.mes.auto.application.event.SmallPackageBoxEvent;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author jzb 2018-06-22
 */
public interface PackageBoxService {

    static String key(LocalDate ld) {
        return "PackageBoxIncr[" + ld + "]";
    }

    static String key() {
        return key(LocalDate.now());
    }

    static String key(Date date) {
        return key(J.localDate(date));
    }

    Single<PackageBox> handle(Principal principal, PackageBoxEvent.ManualCommandSimple command);

    Single<PackageBox> handle(Principal principal, PackageBoxEvent.ManualCommand command);

    Single<PackageBox> handle(Principal principal, PackageBoxEvent.TemporaryBoxCommand command);

    Single<PackageBox> update(Principal principal, String id, PackageBoxMeasureInfoUpdateCommand command);

    Completable print(Principal principal, String id);

    Completable print(Principal principal, PackageBoxBatchPrintUpdateCommand command);

    Completable delete(Principal principal, String id);

    Single<PackageBox> handle(Principal principal, PackageBoxAppendCommand command);

    Flowable<PackageBox> handle(Principal principal, SmallPackageBoxEvent.BatchCommand command);
}
