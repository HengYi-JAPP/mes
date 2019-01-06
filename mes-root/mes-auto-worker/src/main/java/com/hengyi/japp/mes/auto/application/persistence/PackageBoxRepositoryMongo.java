package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.PackageBoxService;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQueryForMeasure;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import com.hengyi.japp.mes.auto.search.lucene.PackageBoxLucene;
import com.mongodb.client.model.Filters;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class PackageBoxRepositoryMongo extends MongoEntityRepository<PackageBox> implements PackageBoxRepository {
    private final RedisClient redisClient;
    private final PackageBoxLucene packageBoxLucene;

    @Inject
    private PackageBoxRepositoryMongo(MongoEntiyManager mongoEntiyManager, RedisClient redisClient, PackageBoxLucene packageBoxLucene) {
        super(mongoEntiyManager);
        this.redisClient = redisClient;
        this.packageBoxLucene = packageBoxLucene;
    }

    @Override
    public Single<PackageBox> save(PackageBox packageBox) {
        if (packageBox.getPrintClass() == null) {
            packageBox.setPrintClass(packageBox.getBudatClass());
//            packageBox.setPrintDate(packageBox.getBudat());
        }
        final Single<PackageBox> single;
        if (J.isBlank(packageBox.getCode()) && packageBox.getPrintDate() != null) {
            single = generateCode(packageBox).map(code -> {
                packageBox.setCode(code);
                return packageBox;
            });
        } else {
            single = Single.just(packageBox);
        }
        return single.flatMap(super::save).doOnSuccess(packageBoxLucene::index);
    }

    private Single<String> generateCode(PackageBox packageBox) {
        final LocalDate ld = J.localDate(packageBox.getPrintDate());
        final long between = ChronoUnit.DAYS.between(LocalDate.now(), ld);
        if (Math.abs(between) >= 365) {
            throw new RuntimeException("时间超出");
        }
        final String incrKey = PackageBoxService.key(ld);
        return redisClient.rxIncr(incrKey).map(l -> {
            final String serialCode = Strings.padStart("" + l, 5, '0');
            final Batch batch = packageBox.getBatch();
            final Workshop workshop = batch.getWorkshop();
            final Corporation corporation = workshop.getCorporation();
            final Product product = batch.getProduct();
            final String corporationPackageCode = corporation.getPackageCode();
            final String productCode = product.getCode();
            final Grade grade = packageBox.getGrade();
            final String gradeCode = grade.getCode();
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
            return corporationPackageCode + productCode + ld.format(dtf) + batch.getBatchNo() + gradeCode + serialCode;
        }).doOnSuccess(it -> updateTtl(incrKey));
    }

    private void updateTtl(String incrKey) {
        redisClient.rxTtl(incrKey)
                // key 存在，但没有设置剩余生存时间
                .filter(it -> it == -1)
                .flatMapSingleElement(it -> {
                    // 一年后过期
                    final long seconds = ChronoUnit.YEARS.getDuration().getSeconds();
                    return redisClient.rxExpire(incrKey, seconds);
                })
                .subscribe();
    }

    @Override
    public Single<PackageBox> findByCode(String code) {
        final JsonObject query = unDeletedQuery(Filters.eq("code", code));
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                .flatMap(this::rxCreateMongoEntiy);
    }

    @Override
    public Single<PackageBoxQuery.Result> query(PackageBoxQuery packageBoxQuery) {
        final int first = packageBoxQuery.getFirst();
        final int pageSize = packageBoxQuery.getPageSize();
        final PackageBoxQuery.Result.ResultBuilder builder = PackageBoxQuery.Result.builder().first(first).pageSize(pageSize);

        return Single.just(packageBoxQuery)
                .map(packageBoxLucene::build)
                .map(it -> packageBoxLucene.baseQuery(it, first, pageSize))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find).toList()
                .map(packageBoxes -> builder.packageBoxes(packageBoxes).build());
    }

    @Override
    public Single<PackageBoxQueryForMeasure.Result> query(PackageBoxQueryForMeasure packageBoxQuery) {
        final int first = packageBoxQuery.getFirst();
        final int pageSize = packageBoxQuery.getPageSize();
        final PackageBoxQueryForMeasure.Result.ResultBuilder builder = PackageBoxQueryForMeasure.Result.builder().first(first).pageSize(pageSize);
        final Sort sort = new Sort(new SortedNumericSortField("createDateTime", SortField.Type.LONG, true));

        return Single.just(packageBoxQuery)
                .map(packageBoxLucene::build)
                .map(it -> packageBoxLucene.baseQuery(it, first, pageSize, sort))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find).toList()
                .map(packageBoxes -> builder.packageBoxes(packageBoxes).build());
    }

}
