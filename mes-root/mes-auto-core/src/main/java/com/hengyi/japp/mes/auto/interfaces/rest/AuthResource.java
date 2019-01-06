package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.AuthService;
import com.hengyi.japp.mes.auto.application.command.TokenCommand;
import io.reactivex.Single;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.security.Principal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("")
@Produces(APPLICATION_JSON)
public class AuthResource {
    private final AuthService authService;

    @Inject
    private AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @Path("token")
    @POST
    @Produces(TEXT_PLAIN)
    public Single<String> token(TokenCommand command) {
        return authService.token(command);
    }

    @Path("api/auth")
    @GET
    public Single<String> auth(Principal principal) {
        return authService.authInfo(principal);
    }
}
