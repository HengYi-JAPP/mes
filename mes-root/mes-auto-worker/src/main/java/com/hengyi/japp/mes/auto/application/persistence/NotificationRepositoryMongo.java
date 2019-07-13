package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ApplicationEvents;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.Notification;
import com.hengyi.japp.mes.auto.repository.NotificationRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class NotificationRepositoryMongo extends MongoEntityRepository<Notification> implements NotificationRepository {
    private final ApplicationEvents applicationEvents;

    @Inject
    private NotificationRepositoryMongo(MongoEntiyManager mongoEntiyManager, ApplicationEvents applicationEvents) {
        super(mongoEntiyManager);
        this.applicationEvents = applicationEvents;
    }

    @Override
    public Single<Notification> save(Notification notification) {
        return super.save(notification).doOnSuccess(applicationEvents::fire);
    }

    @Override
    public Completable delete(String id) {
        return find(id).flatMapCompletable(notification -> {
            notification.setDeleted(true);
            return notification._delete().doOnComplete(() -> applicationEvents.fire(notification));
        });
    }

}
