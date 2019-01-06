package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.SilkNoteUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkNote;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-21
 */
public interface SilkNoteService {

    Single<SilkNote> create(Principal principal, SilkNoteUpdateCommand command);

    Single<SilkNote> update(Principal principal, String id, SilkNoteUpdateCommand command);
}
