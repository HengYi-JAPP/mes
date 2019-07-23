package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Notification;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface NotificationRepository {

    Single<Notification> create();

    Single<Notification> save(Notification notification);

    Single<Notification> find(String id);

    Flowable<Notification> list();

    Completable delete(String id);
}
