package lock;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.application.SilkCarModel;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.SilkCarRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import com.hengyi.japp.mes.auto.worker.Worker;
import com.hengyi.japp.mes.auto.worker.WorkerModule;
import com.hengyi.japp.mes.auto.worker.verticle.WorkerVerticle;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;

import java.util.List;

/**
 * @author jzb 2019-03-03
 */
public class LockTest {

    public static void main(String[] args) {
        System.setProperty("japp.mes.auto.path", "/data/mes-3000/auto");
        final Vertx vertx = Vertx.vertx();
        Worker.INJECTOR = Guice.createInjector(new GuiceModule(vertx), new WorkerModule());
        vertx.rxDeployVerticle(WorkerVerticle.class.getName())
                .ignoreElement()
                .subscribe(LockTest::test);
    }

    private static void test() {
        final SilkCarRepository silkCarRepository = Jvertx.getProxy(SilkCarRepository.class);
        final WorkshopRepository workshopRepository = Jvertx.getProxy(WorkshopRepository.class);
        final Single<SilkCar> silkCar$ = silkCarRepository.findByCode("6000P36763");
        final Single<Workshop> workshop$ = workshopRepository.find("5c8c22cf8070b400017efdbc");
        SilkCarModel.auto(silkCar$, workshop$).flatMap(silkCarModel -> {
            final List<CheckSilkDTO> list = silkCarModel.checkSilks().blockingGet();
            System.out.println(list);

            final CheckSilkDTO checkSilk1 = new CheckSilkDTO();
            checkSilk1.setSideType(SilkCarSideType.B);
            checkSilk1.setRow(3);
            checkSilk1.setCol(2);
            checkSilk1.setCode("00CZ007RF04F");
            final CheckSilkDTO checkSilk2 = new CheckSilkDTO();
            checkSilk2.setSideType(SilkCarSideType.B);
            checkSilk2.setRow(2);
            checkSilk2.setCol(4);
            checkSilk2.setCode("00CZ00HYS05F");
            final CheckSilkDTO checkSilk3 = new CheckSilkDTO();
            checkSilk3.setSideType(SilkCarSideType.A);
            checkSilk3.setRow(1);
            checkSilk3.setCol(5);
            checkSilk3.setCode("00CZ007R803F");
            return silkCarModel.generateSilkRuntimes(List.of(checkSilk1, checkSilk2, checkSilk3));
        }).subscribe(it -> {
            System.out.println(it);
        });
    }
}
