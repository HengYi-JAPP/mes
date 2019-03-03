package lock;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author jzb 2019-03-03
 */
public class LockTest {
    private static final Semaphore semaphore = new Semaphore(1);
    private static final ExecutorService es = Executors.newCachedThreadPool();

    @SneakyThrows
    public static void main(String[] args) {
        es.submit(new LockTask());
        IntStream.rangeClosed(0, 5).mapToObj(Task::new).forEach(es::submit);

        TimeUnit.SECONDS.sleep(5);
        es.submit(new ReleaseTask());
    }

    @Slf4j
    public static class LockTask implements Runnable {
        @Override
        public void run() {
            try {
                semaphore.acquire();
                log.info("enter LockTask.run");
                TimeUnit.DAYS.sleep(1);
                semaphore.release();
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
    }

    @Slf4j
    @Data
    public static class Task implements Runnable {
        private final int i;

        @Override
        public void run() {
            try {
                semaphore.acquire();
                log.info("enter Task[" + i + "].run");
//                semaphore.release();
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
    }

    @Slf4j
    public static class ReleaseTask implements Runnable {
        @Override
        public void run() {
            log.info("enter ReleaseTask.run");
            semaphore.release();
            es.submit(new Task(10));
        }
    }
}
