package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.NotificationService;
import com.hengyi.japp.mes.auto.application.command.NotificationUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Notification;
import com.hengyi.japp.mes.auto.repository.NotificationRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.ws.rs.*;
import java.security.Principal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class NotificationResource {
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Inject
    private NotificationResource(NotificationService notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Path("notifications")
    @POST
    public Single<Notification> create(Principal principal, NotificationUpdateCommand command) {
        return notificationService.create(principal, command);
    }

    @Path("notifications/{id}")
    @PUT
    public Single<Notification> update(Principal principal, @PathParam("id") String id, NotificationUpdateCommand command) {
        return notificationService.update(principal, id, command);
    }

    @Path("notifications/{id}")
    @DELETE
    public Completable delete(Principal principal, @PathParam("id") String id) {
        return notificationService.delete(principal, id);
    }

    @Path("notifications")
    @GET
    public Flowable<Notification> notifications() {
        return notificationRepository.list();
    }

}
