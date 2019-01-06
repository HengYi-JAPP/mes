package com.hengyi.japp.mes.auto.worker;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.Util;
import com.hengyi.japp.mes.auto.application.*;
import com.hengyi.japp.mes.auto.application.persistence.*;
import com.hengyi.japp.mes.auto.application.persistence.disk.EventSourceRepositoryDisk;
import com.hengyi.japp.mes.auto.application.persistence.redis.SilkCarRuntimeRepositoryRedis;
import com.hengyi.japp.mes.auto.interfaces.facevisa.FacevisaService;
import com.hengyi.japp.mes.auto.interfaces.facevisa.internal.FacevisaServiceImpl;
import com.hengyi.japp.mes.auto.interfaces.jikon.JikonAdapter;
import com.hengyi.japp.mes.auto.interfaces.jikon.internal.JikonAdapterImpl;
import com.hengyi.japp.mes.auto.interfaces.riamb.RiambService;
import com.hengyi.japp.mes.auto.interfaces.riamb.internal.RiambServiceImpl;
import com.hengyi.japp.mes.auto.interfaces.warehouse.WarehouseService;
import com.hengyi.japp.mes.auto.interfaces.warehouse.internal.WarehouseServiceImpl;
import com.hengyi.japp.mes.auto.repository.*;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static io.vertx.config.yaml.YamlProcessor.YAML_MAPPER;

/**
 * @author jzb 2018-03-21
 */
public class WorkerModule extends GuiceModule {

    public WorkerModule(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected void configure() {
        bind(ApplicationEvents.class).to(ApplicationEventsRabbit.class);
        bind(AuthService.class).to(AuthServiceImpl.class);

        bind(CorporationService.class).to(CorporationServiceImpl.class);
        bind(WorkshopService.class).to(WorkshopServiceImpl.class);
        bind(LineService.class).to(LineServiceImpl.class);
        bind(LineMachineService.class).to(LineMachineServiceImpl.class);
        bind(GradeService.class).to(GradeServiceImpl.class);
        bind(ProductService.class).to(ProductServiceImpl.class);
        bind(ProductProcessService.class).to(ProductProcessServiceImpl.class);
        bind(SilkExceptionService.class).to(SilkExceptionServiceImpl.class);
        bind(SilkCarService.class).to(SilkCarServiceImpl.class);
        bind(SilkCarRecordService.class).to(SilkCarRecordServiceImpl.class);
        bind(BatchService.class).to(BatchServiceImpl.class);
        bind(ProductPlanNotifyService.class).to(ProductPlanNotifyServiceImpl.class);
        bind(OperatorGroupService.class).to(OperatorGroupServiceImpl.class);
        bind(PermissionService.class).to(PermissionServiceImpl.class);
        bind(OperatorService.class).to(OperatorServiceImpl.class);
        bind(FormConfigService.class).to(FormConfigServiceImpl.class);
        bind(SilkNoteService.class).to(SilkNoteServiceImpl.class);
        bind(SilkCarRuntimeService.class).to(SilkCarRuntimeServiceImpl.class);
        bind(DyeingService.class).to(DyeingServiceImpl.class);
        bind(SilkBarcodeService.class).to(SilkBarcodeServiceImpl.class);
        bind(SilkService.class).to(SilkServiceImpl.class);
        bind(TemporaryBoxService.class).to(TemporaryBoxServiceImpl.class);
        bind(PackageClassService.class).to(PackageClassServiceImpl.class);
        bind(PackageBoxService.class).to(PackageBoxServiceImpl.class);
        bind(ReportService.class).to(ReportServiceImpl.class);
        bind(SapService.class).to(SapServiceImpl.class);
        bind(SapT001lService.class).to(SapT001lServiceImpl.class);

        bind(OperatorRepository.class).to(OperatorRepositoryMongo.class);
        bind(OperatorGroupRepository.class).to(OperatorGroupRepositoryMongo.class);
        bind(CorporationRepository.class).to(CorporationRepositoryMongo.class);
        bind(WorkshopRepository.class).to(WorkshopRepositoryMongo.class);
        bind(LineRepository.class).to(LineRepositoryMongo.class);
        bind(LineMachineRepository.class).to(LineMachineRepositoryMongo.class);
        bind(ProductRepository.class).to(ProductRepositoryMongo.class);
        bind(ProductProcessRepository.class).to(ProductProcessRepositoryMongo.class);
        bind(GradeRepository.class).to(GradeRepositoryMongo.class);
        bind(SilkCarRecordRepository.class).to(SilkCarRecordRepositoryMongo.class);
        bind(BatchRepository.class).to(BatchRepositoryMongo.class);
        bind(SilkCarRepository.class).to(SilkCarRepositoryMongo.class);
        bind(SilkExceptionRepository.class).to(SilkExceptionRepositoryMongo.class);
        bind(PackageBoxRepository.class).to(PackageBoxRepositoryMongo.class);
        bind(PackageBoxFlipRepository.class).to(PackageBoxFlipRepositoryMongo.class);
        bind(EventSourceRepository.class).to(EventSourceRepositoryDisk.class);
        bind(PermissionRepository.class).to(PermissionRepositoryMongo.class);
        bind(ProductPlanLineMachineRepository.class).to(ProductPlanLineMachineRepositoryMongo.class);
        bind(ProductPlanNotifyRepository.class).to(ProductPlanNotifyRepositoryMongo.class);
        bind(LineMachineProductPlanRepository.class).to(LineMachineProductPlanRepositoryMongo.class);
        bind(FormConfigRepository.class).to(FormConfigRepositoryMongo.class);
        bind(SilkNoteRepository.class).to(SilkNoteRepositoryMongo.class);
        bind(SilkRepository.class).to(SilkRepositoryMongo.class);
        bind(DyeingSampleRepository.class).to(DyeingSampleRepositoryMongo.class);
        bind(SilkBarcodeRepository.class).to(SilkBarcodeRepositoryMongo.class);
        bind(DyeingResultRepository.class).to(DyeingResultRepositoryMongo.class);
        bind(DyeingPrepareRepository.class).to(DyeingPrepareRepositoryMongo.class);
        bind(TemporaryBoxRepository.class).to(TemporaryBoxRepositoryMongo.class);
        bind(PackageClassRepository.class).to(PackageClassRepositoryMongo.class);
        bind(SilkCarRuntimeRepository.class).to(SilkCarRuntimeRepositoryRedis.class);
        bind(SapT001lRepository.class).to(SapT001lRepositoryMongo.class);
        bind(TemporaryBoxRecordRepository.class).to(TemporaryBoxRecordRepositoryMongo.class);
        bind(LoginRepository.class).to(LoginRepositoryMongo.class);

        bind(RiambService.class).to(RiambServiceImpl.class);
        bind(JikonAdapter.class).to(JikonAdapterImpl.class);
        bind(FacevisaService.class).to(FacevisaServiceImpl.class);
        bind(WarehouseService.class).to(WarehouseServiceImpl.class);
    }

    @Provides
    @Singleton
    @Named("luceneRootPath")
    private Path luceneRootPath(@Named("autoRootPath") Path autoRootPath) {
        final Path path = Paths.get("db", "lucene");
        return autoRootPath.resolve(path);
    }

    @SneakyThrows
    @Provides
    @Singleton
    @Named("AutolSilkCarModel.Configs")
    private Collection<AutolSilkCarModelConfigRegistry.Config> ConfigRegistry(@Named("autoRootPath") Path autoRootPath) {
        final Path path = autoRootPath.resolve("auto_doffing_config");
        final ImmutableList.Builder<AutolSilkCarModelConfigRegistry.Config> builder = ImmutableList.builder();
        final String[] extensions = {"yml"};
        for (File file : FileUtils.listFiles(path.toFile(), extensions, true)) {
            final AutolSilkCarModelConfigRegistry.Config config = YAML_MAPPER.readValue(file, AutolSilkCarModelConfigRegistry.Config.class);
            config.selfCheck();
            builder.add(config);
        }
        return builder.build();
    }

    @Provides
    @Named("eventSourceRootPath")
    private Path eventSourceRootPath(@Named("autoRootPath") Path autoRootPath) {
        final Path path = Paths.get("db", "event-source");
        return autoRootPath.resolve(path);
    }

    @SneakyThrows
    @Provides
    @Singleton
    @Named("jikonDS")
    private JDBCClient jikonDS(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve("jikon_ds.config.yml");
        final JsonObject config = Util.readJsonObject(configPath);
        return JDBCClient.createShared(vertx, config, "jikonDS");
    }

    @SneakyThrows
    @Provides
    @Singleton
    private RedisClient RedisClient(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve("redis.config.yml");
        final JsonObject config = Util.readJsonObject(configPath);
        final RedisOptions options = new RedisOptions(config);
        return RedisClient.create(vertx, options);
    }

    @SneakyThrows
    @Provides
    private MongoClient MongoClient(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve("mongo.config.yml");
        final JsonObject config = Util.readJsonObject(configPath);
        return MongoClient.createShared(vertx, config);
    }

    @SneakyThrows
    @Provides
    @Singleton
    private MongoDatabase MongoDatabase(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve("mongo.config.yml");
        final JsonObject config = Util.readJsonObject(configPath);
        final ServerAddress serverAddress = new ServerAddress(config.getString("host"), config.getInteger("port"));
        final String db_name = config.getString("db_name", "DEFAULT_DB");

        final String username = config.getString("username");
        if (J.nonBlank(username)) {
            final String authSource = config.getString("authSource");
            final String password = config.getString("password");
            final MongoCredential mongoCredential = MongoCredential.createCredential(username, authSource, password.toCharArray());
            final List<MongoCredential> credentialsList = Lists.newArrayList(mongoCredential);
            return new com.mongodb.MongoClient(serverAddress, credentialsList).getDatabase(db_name);
        }
        return new com.mongodb.MongoClient(serverAddress).getDatabase(db_name);
    }

}
