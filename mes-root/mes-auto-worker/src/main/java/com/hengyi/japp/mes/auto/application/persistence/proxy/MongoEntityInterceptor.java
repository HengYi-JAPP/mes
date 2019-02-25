package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodProxy;
import org.bson.types.ObjectId;

import java.lang.reflect.Method;
import java.util.Collection;


@Slf4j
public class MongoEntityInterceptor<T extends MongoEntity> extends JsonEntityInterceptor<T> {
    private final MongoEntiyManager mongoEntiyManager;
    private final String collectionName;
    private String _id;

    public MongoEntityInterceptor(Class<T> entityClass, Collection<JsonEntityFieldHandler> fieldHandlers, JsonObject originJsonObject, MongoEntiyManager mongoEntiyManager) {
        super(entityClass, fieldHandlers, originJsonObject);
        this.mongoEntiyManager = mongoEntiyManager;
        this.collectionName = MongoUtil.collectionName(entityClass);
        this._id = originJsonObject.getString("_id", new ObjectId().toHexString());
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        final String methodName = method.getName();
        if ("getId".equals(methodName)) {
            return _id;
        }
        if ("setId".equals(methodName)) {
            _id = (String) args[0];
            return null;
        }
        return super.intercept(target, method, args, proxy);
    }

    @Override
    protected Single<String> handleCreate() {
        final MongoClient mongoClient = mongoEntiyManager.getMongoClient();
        final JsonObject document = collectFields().put("_id", _id);
        return mongoClient.rxSave(collectionName, document).defaultIfEmpty(_id).toSingle();
    }

    private JsonObject collectFields() {
        final JsonObject result = new JsonObject();
        fieldHandlers.forEach(it -> {
            final JsonObject field = it.toJsonObject();
            result.getMap().putAll(field.getMap());
        });
        return result;
    }

    @Override
    protected Single<String> handleUpdate() {
        final MongoClient mongoClient = mongoEntiyManager.getMongoClient();
        final JsonObject query = new JsonObject().put("_id", _id);
        final JsonObject collectFields = collectFields();
        if (collectFields.isEmpty()) {
            return Single.just(_id);
        }
        final JsonObject update = new JsonObject().put("$set", collectFields);
        return mongoClient.rxFindOneAndUpdate(collectionName, query, update)
                .ignoreElement()
                .doAfterTerminate(() -> mongoEntiyManager.refresh(entityClass, _id))
                .andThen(Single.just(_id));
    }

    @Override
    protected Completable handleDelete() {
        final MongoClient mongoClient = mongoEntiyManager.getMongoClient();
        final JsonObject query = new JsonObject().put("_id", _id);
        return mongoClient.rxRemoveDocument(collectionName, query).ignoreElement();
    }

}
