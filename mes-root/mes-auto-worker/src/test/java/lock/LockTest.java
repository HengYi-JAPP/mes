package lock;

import com.hengyi.japp.mes.auto.domain.DyeingResult;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.UnicastSubject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author jzb 2019-03-03
 */
@Slf4j
public class LockTest {
    private static final UnicastSubject<DyeingResult> dyeingTimeLineUpdateSubject = UnicastSubject.create();
    private static final Executor exe = Executors.newSingleThreadExecutor();
    private static final Executor exeCache = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        dyeingTimeLineUpdateSubject.subscribeOn(Schedulers.from(exe))
                .subscribe(new Observer<DyeingResult>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(DyeingResult dyeingResult) {
                        System.out.println("onNext : " + Thread.currentThread() + " : " + dyeingResult);
                    }

                    @Override
                    public void onError(Throwable e) {
                        log.error("", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        IntStream.rangeClosed(1, 1000).forEach(i -> {
            exeCache.execute(() -> {
                System.out.println("publish : " + Thread.currentThread());
                dyeingTimeLineUpdateSubject.onNext(new DyeingResult());
            });
        });

        while (true) {

        }
    }
}
