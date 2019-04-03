package com.hengyi.japp.mes.auto.doffing.application.intenal;

import com.github.ixtf.japp.codec.Jcodec;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.doffing.application.LogEntityService;
import com.hengyi.japp.mes.auto.doffing.application.command.LogCreateCommand;
import com.hengyi.japp.mes.auto.doffing.domain.LogEntity;
import io.reactivex.Completable;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.util.Date;

import static com.hengyi.japp.mes.auto.doffing.Doffing.runInTx;

/**
 * @author jzb 2019-03-08
 */
@Slf4j
@Singleton
public class LogEntityServiceImpl implements LogEntityService {
    private final EntityManager em;

    @Inject
    private LogEntityServiceImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Completable create(LogCreateCommand command) {
        return Completable.fromAction(() -> runInTx(em, () -> {
            final LogEntity logEntity = new LogEntity();
            logEntity.setId(Jcodec.uuid58());
            logEntity.setCreateDateTime(new Date());
            logEntity.setLevel(command.getLevel());
            logEntity.setMessage(command.getMessage());
            logEntity.setCommand(command.getCommand());
            logEntity.setOperatorName(command.getOperatorName());
            em.merge(logEntity);
        }));
    }

}
