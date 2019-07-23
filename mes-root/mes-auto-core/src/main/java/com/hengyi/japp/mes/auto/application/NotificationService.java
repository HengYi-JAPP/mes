package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.NotificationUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Notification;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface NotificationService {

    Single<Notification> create(Principal principal, NotificationUpdateCommand command);

    Single<Notification> update(Principal principal, String id, NotificationUpdateCommand command);

    Completable delete(Principal principal, String id);
}
