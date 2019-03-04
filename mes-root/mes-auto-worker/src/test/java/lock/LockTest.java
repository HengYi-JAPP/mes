package lock;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.codec.Jcodec;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.application.SilkBarcodeService;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-03-03
 */
public class LockTest {
    private static final Semaphore semaphore = new Semaphore(1);
    private static final ExecutorService es = Executors.newCachedThreadPool();

    @SneakyThrows
    public static void main(String[] args) {
        String s = "[ { \"code\": \"005X006TO02B\" }, { \"code\": \"005X006TT14B\" }, { \"code\": \"005X006TT22B\" }, { \"code\": \"005X006TT24B\" }, { \"code\": \"005X006TT21B\" }, { \"code\": \"005X007AX21B\" }, { \"code\": \"005X007DJ19B\" }, { \"code\": \"005X007EB12B\" }, { \"code\": \"005X007EB14B\" }, { \"code\": \"005X007EB18B\" }, { \"code\": \"005X007EB20B\" }, { \"code\": \"005U006FL12B\" }, { \"code\": \"005X007DK19B\" }, { \"code\": \"005X007DK11B\" }, { \"code\": \"005X007DK20B\" }, { \"code\": \"005X007DJ20B\" }, { \"code\": \"005X007E108B\" }, { \"code\": \"005X006TT10B\" }, { \"code\": \"005X007EA18B\" }, { \"code\": \"005X007EA20B\" }, { \"code\": \"005X007EA19B\" }, { \"code\": \"005X007EA21B\" }, { \"code\": \"005X007E008B\" }, { \"code\": \"005X007AX19B\" }, { \"code\": \"005X007EA17B\" }, { \"code\": \"005X002R509B\" }, { \"code\": \"005X007DJ11B\" }, { \"code\": \"005X007EB19B\" }, { \"code\": \"005X007E920B\" }, { \"code\": \"005X007E918B\" }, { \"code\": \"005X002Q521B\" }, { \"code\": \"005S004XC04B\" }, { \"code\": \"005X002RJ09B\" }, { \"code\": \"005X002RE04B\" }, { \"code\": \"005X007E921B\" }, { \"code\": \"005S004XC12B\" }, { \"code\": \"005X002QA22B\" }, { \"code\": \"005X002RE21B\" }, { \"code\": \"005X002QA15B\" }, { \"code\": \"005X0037S19B\" }, { \"code\": \"005X003AX19B\" }, { \"code\": \"005X002PQ22B\" }, { \"code\": \"005S004XC02B\" }, { \"code\": \"005X007E912B\" }, { \"code\": \"005X007EB21B\" }, { \"code\": \"005X002R809B\" }, { \"code\": \"005S004XC03B\" }, { \"code\": \"005X007E914B\" } ]";
        final HashSet<String> set = Sets.newHashSet();
        JsonNode jsonNode = MAPPER.readValue(s, JsonNode.class);
        for (JsonNode node : jsonNode) {
            String code = node.get("code").asText();
            String silkBarCode = SilkBarcodeService.silkCodeToSilkBarCode(code);
            set.add(silkBarCode);
        }
        set.forEach(System.out::println);
        System.out.println(jsonNode);

        System.out.println(Jcodec.password("123456"));

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
