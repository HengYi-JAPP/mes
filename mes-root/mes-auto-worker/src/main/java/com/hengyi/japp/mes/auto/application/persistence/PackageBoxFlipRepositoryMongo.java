package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.PackageBoxFlipQuery;
import com.hengyi.japp.mes.auto.domain.PackageBoxFlip;
import com.hengyi.japp.mes.auto.repository.PackageBoxFlipRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import com.hengyi.japp.mes.auto.search.lucene.PackageBoxFlipLucene;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class PackageBoxFlipRepositoryMongo extends MongoEntityRepository<PackageBoxFlip> implements PackageBoxFlipRepository {
    private final PackageBoxFlipLucene packageBoxFlipLucene;

    @Inject
    private PackageBoxFlipRepositoryMongo(MongoEntiyManager mongoEntiyManager, PackageBoxFlipLucene packageBoxFlipLucene) {
        super(mongoEntiyManager);
        this.packageBoxFlipLucene = packageBoxFlipLucene;
    }

    @Override
    public Single<PackageBoxFlip> save(PackageBoxFlip packageBoxFlip) {
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);
        return Flowable.fromIterable(packageBoxFlip.getInSilks()).flatMapSingle(silk -> {
            silk.setPackageDateTime(null);
            silk.setPackageBox(null);
            silk.setDetached(true);
            return silkRepository.save(silk);
        }).toList().flatMap(inSilks -> {
            packageBoxFlip.setInSilks(inSilks);
            return Flowable.fromIterable(packageBoxFlip.getOutSilks()).flatMapSingle(silk -> {
                silk.setPackageDateTime(packageBoxFlip.getCreateDateTime());
                silk.setPackageBox(packageBoxFlip.getPackageBox());
                return silkRepository.save(silk);
            }).toList();
        }).flatMap(outSilks -> {
            packageBoxFlip.setOutSilks(outSilks);
            return super.save(packageBoxFlip);
        }).doOnSuccess(packageBoxFlipLucene::index);
    }

    @Override
    public Single<PackageBoxFlipQuery.Result> query(PackageBoxFlipQuery packageBoxFlipQuery) {
        final int first = packageBoxFlipQuery.getFirst();
        final int pageSize = packageBoxFlipQuery.getPageSize();
        final PackageBoxFlipQuery.Result.ResultBuilder builder = PackageBoxFlipQuery.Result.builder().first(first).pageSize(pageSize);

        return Single.just(packageBoxFlipQuery)
                .map(packageBoxFlipLucene::build)
                .map(it -> packageBoxFlipLucene.baseQuery(it, first, pageSize))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find).toList()
                .map(packageBoxes -> builder.packageBoxFlips(packageBoxes).build());
    }
}
