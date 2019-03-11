package doffing;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.doffing.DoffingModule;
import com.hengyi.japp.mes.auto.doffing.application.DoffingService;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdapt;
import io.vertx.reactivex.core.Vertx;

import javax.persistence.EntityManager;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.doffing.Doffing.INJECTOR;

/**
 * @author jzb 2019-03-08
 */
public class DoffingTest {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new DoffingModule(vertx));
        final DoffingService doffingService = INJECTOR.getInstance(DoffingService.class);
//        doffingService.fetch().flatMapSingle(doffingService::toMessageBody)
//                .subscribe(System.out::println);
//        restore();
    }

    private static void restore() {
        final EntityManager em = INJECTOR.getInstance(EntityManager.class);
        em.getTransaction().begin();
        em.createQuery("select o from AutoDoffingSilkCarRecordAdaptHistory o").getResultList().forEach(it -> {
            final var data = MAPPER.convertValue(it, AutoDoffingSilkCarRecordAdapt.class);
            data.setModifyDateTime(null);
            data.setState(0);
            em.merge(data);
            em.remove(it);
        });
        em.getTransaction().commit();
    }

}
