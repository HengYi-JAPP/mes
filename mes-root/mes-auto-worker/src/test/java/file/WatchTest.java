package file;

import lombok.SneakyThrows;

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author jzb 2019-03-15
 */
public class WatchTest {
    @SneakyThrows
    public static void main(String[] args) {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Paths.get("/home/mes/auto/auto_doffing_config").register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
            }
            key.reset();
        }
    }
}
