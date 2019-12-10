package com.hengyi.japp.mes.auto.interfaces.search.internal;

import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQueryByEventSourceCanHappen;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.interfaces.search.SearchService;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.ExceptionHandlers;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.SendOptions;
import reactor.rabbitmq.Sender;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.Constant.AMQP.MES_AUTO_SEARCH_INDEX_QUEUE;
import static com.hengyi.japp.mes.auto.Constant.AMQP.MES_AUTO_SEARCH_REMOVE_QUEUE;

/**
 * @author jzb 2019-11-14
 */
@Slf4j
@Singleton
public class SearchServiceImpl implements SearchService {
    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private static final SendOptions sendOptions = new SendOptions().exceptionHandler(
            new ExceptionHandlers.RetrySendingExceptionHandler(
                    Duration.ofHours(1), Duration.ofMinutes(5),
                    ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE
            )
    );
    private final String searchBaseUrl;
    private final Sender sender;

    @Inject
    private SearchServiceImpl(MesAutoConfig mesAutoConfig, Sender sender) {
        final JsonObject searchConfig = mesAutoConfig.getSearchOptions();
        final String host = searchConfig.getString("host", "10.2.0.214");
        final Integer port = searchConfig.getInteger("port", 9999);
        searchBaseUrl = "http://" + host + ":" + port;
        this.sender = sender;
    }

    @Override
    public String url(String url) {
        return searchBaseUrl + "/search/" + url;
    }


    @Override
    public Mono<Void> index(LuceneCommandOne command) {
        final Mono<OutboundMessage> message$ = Mono.fromCallable(() -> {
            final byte[] body = MAPPER.writeValueAsBytes(command);
            return new OutboundMessage("", MES_AUTO_SEARCH_INDEX_QUEUE, body);
        }).subscribeOn(Schedulers.elastic());
        return sender.send(message$, sendOptions)
                .doOnError(err -> log.error(command.getClassName() + "[" + command.getId() + "]", err));
    }

    @Override
    public Mono<Void> remove(LuceneCommandOne command) {
        final Mono<OutboundMessage> message$ = Mono.fromCallable(() -> {
            final byte[] body = MAPPER.writeValueAsBytes(command);
            return new OutboundMessage("", MES_AUTO_SEARCH_REMOVE_QUEUE, body);
        }).subscribeOn(Schedulers.elastic());
        return sender.send(message$, sendOptions)
                .doOnError(err -> log.error(command.getClassName() + "[" + command.getId() + "]", err));
    }

    @SneakyThrows({IOException.class, InterruptedException.class})
    @Override
    public Pair<Long, Collection<String>> query(String uri, Object query) {
        final byte[] bytes = MAPPER.writeValueAsBytes(query);
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(BodyPublishers.ofByteArray(bytes))
                .build();
        final HttpResponse<byte[]> response = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
        final int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException();
        }
        return MAPPER.readValue(response.body(), QueryResult.class).pair();
    }

    @SneakyThrows({IOException.class, InterruptedException.class})
    @Override
    public Collection<String> query(SilkCarRecordQueryByEventSourceCanHappen query) {
        final byte[] bytes = MAPPER.writeValueAsBytes(query);
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url("silkCarRecordsByEventSourceCanHappen")))
                .POST(BodyPublishers.ofByteArray(bytes))
                .build();
        final HttpResponse<byte[]> response = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
        final int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException();
        }
        final CollectionLikeType collectionLikeType = MAPPER.getTypeFactory().constructCollectionLikeType(ArrayList.class, String.class);
        return MAPPER.readValue(response.body(), collectionLikeType);
    }

}
