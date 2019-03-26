package mongo;

import lombok.SneakyThrows;

import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author jzb 2018-06-20
 */
public class MongoTest {
    public static final Path path = Paths.get("/home/mes/auto/doffing_spec");

    @SneakyThrows
    public static void main(String[] args) {
        Executors.newSingleThreadExecutor().submit(MongoTest::watchDoffingSpec);

        TimeUnit.DAYS.sleep(1);
    }

    @SneakyThrows
    private static void watchDoffingSpec() {
        final WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        WatchKey key;
        while ((key = watchService.take()) != null) {
            System.out.println("change");
            key.pollEvents();
            key.reset();
        }
    }

}
