package com.hengyi.japp.mes.auto.worker;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.application.*;
import com.hengyi.japp.mes.auto.application.persistence.*;
import com.hengyi.japp.mes.auto.application.persistence.disk.EventSourceRepositoryDisk;
import com.hengyi.japp.mes.auto.application.persistence.redis.SilkCarRuntimeRepositoryRedis;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.interfaces.facevisa.FacevisaService;
import com.hengyi.japp.mes.auto.interfaces.facevisa.internal.FacevisaServiceImpl;
import com.hengyi.japp.mes.auto.interfaces.jikon.JikonAdapter;
import com.hengyi.japp.mes.auto.interfaces.jikon.internal.JikonAdapterImpl;
import com.hengyi.japp.mes.auto.interfaces.riamb.RiambService;
import com.hengyi.japp.mes.auto.interfaces.riamb.internal.RiambServiceImpl;
import com.hengyi.japp.mes.auto.interfaces.ruiguan.RuiguanService;
import com.hengyi.japp.mes.auto.interfaces.ruiguan.internal.RuiguanServiceImpl;
import com.hengyi.japp.mes.auto.interfaces.warehouse.WarehouseService;
import com.hengyi.japp.mes.auto.interfaces.warehouse.internal.WarehouseServiceImpl;
import com.hengyi.japp.mes.auto.repository.*;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;


/**
 * @author jzb 2018-03-21
 */
public class WorkerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationEvents.class).to(ApplicationEventsRabbit.class);
        bind(AuthService.class).to(AuthServiceImpl.class);
        bind(AdminService.class).to(AdminServiceImpl.class);

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
        bind(SapService.class).to(SapServiceImpl.class);
        bind(SapT001lService.class).to(SapT001lServiceImpl.class);
        bind(DoffingSpecService.class).to(DoffingSpecServiceCustom.class);
        bind(SilkBarcodeGenerateTemplateService.class).to(SilkBarcodeGenerateTemplateServiceImpl.class);
        bind(ExceptionRecordService.class).to(ExceptionRecordServiceImpl.class);
        bind(NotificationService.class).to(NotificationServiceImpl.class);

        bind(DictionaryService.class).to(DictionaryServiceImpl.class);
        bind(DictionaryRepository.class).to(DictionaryRepositoryMongo.class);

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
        bind(SilkBarcodeGenerateTemplateRepository.class).to(SilkBarcodeGenerateTemplateRepositoryMongo.class);
        bind(ExceptionRecordRepository.class).to(ExceptionRecordRepositoryMongo.class);
        bind(NotificationRepository.class).to(NotificationRepositoryMongo.class);

        bind(RuiguanService.class).to(RuiguanServiceImpl.class);
        bind(RiambService.class).to(RiambServiceImpl.class);
        bind(JikonAdapter.class).to(JikonAdapterImpl.class);
        bind(FacevisaService.class).to(FacevisaServiceImpl.class);
        bind(WarehouseService.class).to(WarehouseServiceImpl.class);
    }

    @SneakyThrows
    @Provides
    @Singleton
    @Named("AutolSilkCarModel.Configs")
    private Collection<AutoSilkCarModelConfigRegistry.Config> ConfigRegistry(MesAutoConfig config) {
        final Path path = config.getRootPath().resolve("auto_doffing_config");
        final ImmutableList.Builder<AutoSilkCarModelConfigRegistry.Config> builder = ImmutableList.builder();
        final String[] extensions = {"yml"};
        for (File file : FileUtils.listFiles(path.toFile(), extensions, true)) {
            final AutoSilkCarModelConfigRegistry.Config doffingConfig = YAML_MAPPER.readValue(file, AutoSilkCarModelConfigRegistry.Config.class);
            doffingConfig.selfCheck();
            builder.add(doffingConfig);
        }
        return builder.build();
    }

    @Provides
    @Named("eventSourceRootPath")
    private Path eventSourceRootPath(MesAutoConfig config) {
        final Path path = Paths.get("db", "event-source");
        return config.getRootPath().resolve(path);
    }

    @SneakyThrows
    @Provides
    @Singleton
    private RedisClient RedisClient(Vertx vertx, MesAutoConfig config) {
        final RedisOptions options = config.getRedisOptions();
        return RedisClient.create(vertx, options);
    }

    @SneakyThrows
    @Provides
    private MongoClient MongoClient(Vertx vertx, MesAutoConfig config) {
        final JsonObject mongoOptions = config.getMongoOptions();
        return MongoClient.createShared(vertx, mongoOptions);
    }

    @SneakyThrows
    @Provides
    @Singleton
    private MongoDatabase MongoDatabase(MesAutoConfig config) {
        final JsonObject mongoOptions = config.getMongoOptions();
        final ServerAddress serverAddress = new ServerAddress(mongoOptions.getString("host"), mongoOptions.getInteger("port"));
        final String db_name = mongoOptions.getString("db_name", "mes-auto");

        return Optional.ofNullable(mongoOptions.getString("username"))
                .filter(J::nonBlank)
                .map(username -> {
                    final String authSource = mongoOptions.getString("authSource");
                    final String password = mongoOptions.getString("password");
                    final MongoCredential mongoCredential = MongoCredential.createCredential(username, authSource, password.toCharArray());
                    return new com.mongodb.MongoClient(serverAddress, mongoCredential, MongoClientOptions.builder().build());
                })
                .orElseGet(() -> new com.mongodb.MongoClient(serverAddress))
                .getDatabase(db_name);
    }

}
