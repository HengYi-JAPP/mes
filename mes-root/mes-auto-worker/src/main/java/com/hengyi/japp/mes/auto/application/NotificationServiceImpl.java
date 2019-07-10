package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.NotificationUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Notification;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.NotificationRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private NotificationServiceImpl(NotificationRepository notificationRepository, WorkshopRepository workshopRepository, LineRepository lineRepository, OperatorRepository operatorRepository) {
        this.notificationRepository = notificationRepository;
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<Notification> create(Principal principal, NotificationUpdateCommand command) {
        return notificationRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Notification> save(Principal principal, Notification notification, NotificationUpdateCommand command) {
        notification.setNote(command.getNote());
        return operatorRepository.find(principal).flatMap(operator -> {
            notification.log(operator);
            return Flowable.fromIterable(J.emptyIfNull(command.getWorkshops()))
                    .flatMapSingle(workshopRepository::find).toList();
        }).flatMap(workshops -> {
            notification.setWorkshops(workshops);
            return Flowable.fromIterable(J.emptyIfNull(command.getLines()))
                    .flatMapSingle(lineRepository::find).toList();
        }).flatMap(lines -> {
            notification.setLines(lines);
            return notificationRepository.save(notification);
        });
    }

    @Override
    public Single<Notification> update(Principal principal, String id, NotificationUpdateCommand command) {
        return notificationRepository.find(id).flatMap(it -> save(principal, it, command));
    }

    @Override
    public Completable delete(Principal principal, String id) {
        return notificationRepository.find(id).flatMapCompletable(Notification::_delete);
    }

}
