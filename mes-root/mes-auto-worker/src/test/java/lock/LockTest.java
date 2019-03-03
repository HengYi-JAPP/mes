package lock;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.codec.Jcodec;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
        String s = "[{\"eventId\":\"5c7ad972b8244000017766d2\",\"type\":\"DyeingPrepareEvent\",\"operator\":{\"id\":\"5c6e03cc3d0045000168e58b\"},\"fireDateTime\":1551554930523,\"deleted\":false,\"deleteOperator\":null,\"deleteDateTime\":null,\"dyeingPrepare\":{\"id\":\"5c7ad972b8244000017766d3\"},\"command\":null},{\"eventId\":\"5c7aea7db824400001b61369\",\"type\":\"ProductProcessSubmitEvent\",\"operator\":{\"id\":\"5c6e03313d00450001679033\"},\"fireDateTime\":1551559293397,\"deleted\":false,\"deleteOperator\":null,\"deleteDateTime\":null,\"productProcess\":{\"id\":\"5c0084c751e9c40001573af7\"},\"silkRuntimes\":[],\"silkExceptions\":[],\"silkNotes\":[],\"formConfig\":null,\"formConfigValueData\":null},{\"eventId\":\"5c7ba4454098e800010a9410\",\"type\":\"SilkRuntimeDetachEvent\",\"operator\":{\"id\":\"5c04a044c3cae813b530cdd1\"},\"fireDateTime\":1551606853402,\"deleted\":false,\"deleteOperator\":null,\"deleteDateTime\":null,\"silkRuntimes\":[{\"sideType\":\"A\",\"row\":1,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e905d\"}},{\"sideType\":\"A\",\"row\":2,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e9055\"}},{\"sideType\":\"A\",\"row\":1,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e905c\"}},{\"sideType\":\"A\",\"row\":1,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e905a\"}},{\"sideType\":\"A\",\"row\":1,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e905b\"}},{\"sideType\":\"A\",\"row\":1,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e905e\"}},{\"sideType\":\"A\",\"row\":2,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e9052\"}},{\"sideType\":\"A\",\"row\":2,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e9056\"}},{\"sideType\":\"A\",\"row\":4,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e9049\"}},{\"sideType\":\"A\",\"row\":3,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e9051\"}},{\"sideType\":\"A\",\"row\":3,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e904e\"}},{\"sideType\":\"A\",\"row\":4,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e9044\"}},{\"sideType\":\"A\",\"row\":3,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e9050\"}},{\"sideType\":\"A\",\"row\":4,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e9047\"}},{\"sideType\":\"A\",\"row\":2,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e9058\"}},{\"sideType\":\"A\",\"row\":3,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e904c\"}},{\"sideType\":\"A\",\"row\":3,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e904d\"}},{\"sideType\":\"B\",\"row\":1,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e9078\"}},{\"sideType\":\"A\",\"row\":4,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e904b\"}},{\"sideType\":\"B\",\"row\":1,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e907b\"}},{\"sideType\":\"A\",\"row\":1,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e905f\"}},{\"sideType\":\"B\",\"row\":1,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e907a\"}},{\"sideType\":\"B\",\"row\":1,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e9079\"}},{\"sideType\":\"B\",\"row\":2,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e906e\"}},{\"sideType\":\"A\",\"row\":2,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e9057\"}},{\"sideType\":\"A\",\"row\":4,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e904a\"}},{\"sideType\":\"B\",\"row\":1,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e9076\"}},{\"sideType\":\"B\",\"row\":2,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e9071\"}},{\"sideType\":\"A\",\"row\":3,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e904f\"}},{\"sideType\":\"A\",\"row\":2,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e9059\"}},{\"sideType\":\"A\",\"row\":4,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e9048\"}},{\"sideType\":\"B\",\"row\":1,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e9077\"}},{\"sideType\":\"B\",\"row\":2,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e9072\"}},{\"sideType\":\"B\",\"row\":2,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e9074\"}},{\"sideType\":\"B\",\"row\":2,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e9075\"}},{\"sideType\":\"B\",\"row\":3,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e906d\"}},{\"sideType\":\"B\",\"row\":3,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e906b\"}},{\"sideType\":\"B\",\"row\":2,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e9073\"}},{\"sideType\":\"B\",\"row\":3,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e906c\"}},{\"sideType\":\"B\",\"row\":3,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e9069\"}},{\"sideType\":\"B\",\"row\":4,\"col\":1,\"silk\":{\"id\":\"5c7ad6afb8244000016e9060\"}},{\"sideType\":\"B\",\"row\":3,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e9068\"}},{\"sideType\":\"B\",\"row\":3,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e906a\"}},{\"sideType\":\"B\",\"row\":4,\"col\":2,\"silk\":{\"id\":\"5c7ad6afb8244000016e9063\"}},{\"sideType\":\"B\",\"row\":4,\"col\":3,\"silk\":{\"id\":\"5c7ad6afb8244000016e9064\"}},{\"sideType\":\"B\",\"row\":4,\"col\":6,\"silk\":{\"id\":\"5c7ad6afb8244000016e9067\"}},{\"sideType\":\"B\",\"row\":4,\"col\":5,\"silk\":{\"id\":\"5c7ad6afb8244000016e9066\"}},{\"sideType\":\"B\",\"row\":4,\"col\":4,\"silk\":{\"id\":\"5c7ad6afb8244000016e9065\"}}]}]";
        JsonNode jsonNode = MAPPER.readValue(s, JsonNode.class);
        for (JsonNode node : jsonNode) {
            String key = "EventSource." + node.get("eventId").asText();
            String value = node.toString();
            System.out.println(key);
            System.out.println(value);
        }
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
