package com.hengyi.japp.mes.auto.doffing.application.intenal;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.doffing.DoffingModuleConfig;
import com.hengyi.japp.mes.auto.doffing.application.DoffingService;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdapt;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdaptHistory;
import com.hengyi.japp.mes.auto.doffing.dto.MessageBoy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-03-08
 */
@Slf4j
@Singleton
public class DoffingServiceImpl implements DoffingService {
    private final static String[] SIDES = new String[]{"A", "B"};
    private final EntityManager em;
    private final DoffingModuleConfig config;

    @Inject
    private DoffingServiceImpl(EntityManager em, DoffingModuleConfig config) {
        this.em = em;
        this.config = config;
    }

    private <T> T callInTx(Callable<T> callable) {
        final EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            final T result = callable.call();
            transaction.commit();
            return result;
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Flowable<AutoDoffingSilkCarRecordAdapt> fetch() {
        log.debug("fetch");
        return Single.just(em.createNamedQuery("Current.fetchData", AutoDoffingSilkCarRecordAdapt.class))
                .flattenAsFlowable(Query::getResultList);
    }

    @Override
    public Single<String> toMessageBody(AutoDoffingSilkCarRecordAdapt data) {
        return Single.fromCallable(() -> {
            final int rowNum = data.getRowNum();
            final int colNum = data.getColNum();
            final MessageBoy messageBoy = new MessageBoy();
            messageBoy.setId(data.getId());
            messageBoy.setPrincipalName(config.getPrincipalName());
            final long l = data.getCreateDateTime() * 1000;
            messageBoy.setCreateDateTime(new Date(l));
            final MessageBoy.SilkCarInfo silkCarInfo = new MessageBoy.SilkCarInfo();
            silkCarInfo.setCode(data.getCode());
            silkCarInfo.setRow(rowNum);
            silkCarInfo.setCol(colNum);
            silkCarInfo.setBatchNo(data.getBatch());
            silkCarInfo.setGrade(data.getGrade());
            messageBoy.setSilkCarInfo(silkCarInfo);
            final List<MessageBoy.SilkInfo> silkInfos = Lists.newArrayList();
            messageBoy.setSilkInfos(silkInfos);
            for (String side : SIDES) {
                for (int row = 1; row <= rowNum; row++) {
                    for (int col = 1; col <= colNum; col++) {
                        //silks_A_1_1
                        final String name = String.join("_", "silks", side, "" + row, "" + col);
                        final String json = (String) PropertyUtils.getProperty(data, name);
                        final var silkInfo = MAPPER.readValue(json, MessageBoy.SilkInfo.class);
                        silkInfo.setSideType(side);
                        silkInfo.setRow(row);
                        silkInfo.setCol(col);
                        silkInfos.add(silkInfo);
                    }
                }
            }
            return MAPPER.writeValueAsString(messageBoy);
        });
    }

    @Override
    public Completable restore(String id) {
        return Completable.fromAction(() -> {
            final var history = em.find(AutoDoffingSilkCarRecordAdaptHistory.class, id);
            if (history != null) {
                final var current = MAPPER.convertValue(history, AutoDoffingSilkCarRecordAdapt.class);
                current.setState(0);
                em.merge(current);
                em.remove(history);
            }
        });
    }

    @Override
    public Single<AutoDoffingSilkCarRecordAdaptHistory> toHistory(AutoDoffingSilkCarRecordAdapt data) {
        return Single.fromCallable(() -> {
            final var history = MAPPER.convertValue(data, AutoDoffingSilkCarRecordAdaptHistory.class);
            history.setModifyDateTime(new Date().getTime() / 1000);
            history.setState(1);
            return callInTx(() -> {
                final var result = em.merge(history);
                em.merge(data);
                em.remove(data);
                return result;
            });
        });
    }

    @Override
    public Completable clean() {
        return Completable.fromAction(() -> {
            final long cleanDelay = config.getCleanDelayDays();
            final long cleanDelaySeconds = TimeUnit.DAYS.toSeconds(cleanDelay);
            final long currentTimestamp = new Date().getTime() / 1000;
            final long cleanTimestamp = currentTimestamp - cleanDelaySeconds;
            em.createNamedQuery("History.fetchCleanData", AutoDoffingSilkCarRecordAdaptHistory.class)
                    .setParameter("cleanTimestamp", cleanTimestamp)
                    .getResultList()
                    .forEach(this::clean);
        });
    }

    private void clean(AutoDoffingSilkCarRecordAdaptHistory history) {
//            em.remove(history);
    }
}
