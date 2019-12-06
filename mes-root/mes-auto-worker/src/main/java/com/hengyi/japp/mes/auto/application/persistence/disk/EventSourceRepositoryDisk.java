package com.hengyi.japp.mes.auto.application.persistence.disk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.repository.EventSourceRepository;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-07-10
 */
@Slf4j
@Singleton
public class EventSourceRepositoryDisk implements EventSourceRepository {
    private final Path eventSourceRootPath;

    @Inject
    private EventSourceRepositoryDisk(@Named("eventSourceRootPath") Path eventSourceRootPath) {
        this.eventSourceRootPath = eventSourceRootPath;
    }

    @Override
    public Single<? extends EventSource> find(String filePath) {
        final File file = FileUtils.getFile(filePath);
        return EventSource.from(file);
    }

    @Override
    public <T extends EventSource> Single<T> save(T event) {
        return Single.fromCallable(() -> {
            final String id = event.getEventId();
            final File file = FileUtils.getFile(getDir(), id + ".json");
            MAPPER.writeValue(file, event);
            return event;
        });
    }

    @Override
    public void handlePersistence(Message<JsonObject> message) {
        final JsonObject jsonObject = message.body();
        final String body = jsonObject.getString("body");

//        Single.fromCallable(() -> save(body))
//                .doOnSuccess(it -> rabbitClient.rxBasicAck(jsonObject.getLong("deliveryTag"), false))
//                .

//        final File dir = getDir();
//        FileUtils.getFile(dir,);
//        FileUtils.write();
//
//        updateRecord(Util.readCommand(CarStatusFinishCommand.class, body))
//                .toCompletable()
//                .andThen(rabbitClient.rxBasicAck(jsonObject.getLong("deliveryTag"), false))
//                .subscribe(it -> {
//                    log.debug("engine 结束处理消息");
//                });
    }

    private File getDir() throws IOException {
        final LocalDate ld = LocalDate.now();
        final File dir = eventSourceRootPath.toFile();
        final File result = FileUtils.getFile(dir, "" + ld.getYear(), "" + ld.getMonthValue(), "" + ld.getDayOfMonth());
        FileUtils.forceMkdir(result);
        return result;
    }

}
