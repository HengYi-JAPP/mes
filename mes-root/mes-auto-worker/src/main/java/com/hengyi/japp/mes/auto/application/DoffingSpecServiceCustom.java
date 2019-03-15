package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-03-15
 */
@Slf4j
@Singleton
public class DoffingSpecServiceCustom implements DoffingSpecService {
    private final Path doffingSpecPath;
    private final SilkBarcodeService silkBarcodeService;
    private final SilkRepository silkRepository;
    private Collection<DoffingSpec> doffingSpecs;

    @SneakyThrows
    @Inject
    private DoffingSpecServiceCustom(MesAutoConfig config, SilkBarcodeService silkBarcodeService, SilkRepository silkRepository) {
        doffingSpecPath = config.getDoffingSpecPath();
        this.silkBarcodeService = silkBarcodeService;
        this.silkRepository = silkRepository;
        doffingSpecs = fetchDoffingSpec();
        watchDoffingSpec();
    }

    @Override
    public List<CheckSilkDTO> checkSilks(DoffingType doffingType, Line line, SilkCar silkCar) {
        final DoffingSpec doffingSpec = findDoffingSpec(doffingType, line, silkCar);
        return doffingSpec.silkCarPositionCheckSpecs.stream().map(it -> {
            final var silkCarPositions = Lists.newArrayList(it.getSilkCarPositions());
            Collections.shuffle(silkCarPositions);
            final var position = silkCarPositions.get(0);
            final CheckSilkDTO result = new CheckSilkDTO();
            result.setSideType(position.getSideType());
            result.setRow(position.getRow());
            result.setCol(position.getCol());
            return result;
        }).collect(toList());
    }

    @Override
    public Flowable<SilkRuntime> generateSilkRuntimes(DoffingType doffingType, Line line, SilkCar silkCar, List<CheckSilkDTO> checkSilks) {
        final DoffingSpec doffingSpec = findDoffingSpec(doffingType, line, silkCar);
        return Flowable.fromIterable(checkSilks).map(CheckSilkDTO::getCode)
                .flatMapSingle(silkBarcodeService::findBySilkCode).toList()
                .flatMapPublisher(silkBarcodes -> {
                    final List<Single<SilkRuntime>> silkRuntimeList$ = Lists.newArrayList();
                    silkBarcodes = sort(silkBarcodes, checkSilks);
                    for (int i = 0; i < silkBarcodes.size(); i++) {
                        final SilkBarcode silkBarcode = silkBarcodes.get(i);
                        final LineMachine lineMachine = silkBarcode.getLineMachine();
                        final Batch batch = silkBarcode.getBatch();
                        final LineMachineSpec lineMachineSpec = doffingSpec.lineMachineSpecs.get(i);
                        for (LineMachineSilkSpec silkSpec : lineMachineSpec.lineMachineSilkSpecs) {
                            final SilkRuntime silkRuntime = new SilkRuntime();
                            silkRuntime.setSideType(silkSpec.getSideType());
                            silkRuntime.setRow(silkSpec.getRow());
                            silkRuntime.setCol(silkSpec.getCol());
                            final var silkRuntime$ = silkRepository.create().map(silk -> {
                                silkRuntime.setSilk(silk);
                                silk.setBatch(batch);
                                silk.setLineMachine(lineMachine);
                                final int spindle = silkSpec.getSpindle();
                                silk.setSpindle(spindle);
                                final String silkCode = silkBarcode.generateSilkCode(spindle);
                                silk.setCode(silkCode);
                                return silkRuntime;
                            });
                            silkRuntimeList$.add(silkRuntime$);
                        }
                    }
                    return Single.merge(silkRuntimeList$);
                });
    }

    private List<SilkBarcode> sort(List<SilkBarcode> silkBarcodes, List<CheckSilkDTO> checkSilks) {
        final var map = silkBarcodes.parallelStream().collect(toMap(SilkBarcode::getCode, Function.identity()));
        return checkSilks.stream().map(dto -> {
            @NotBlank final String silkCode = dto.getCode();
            final String code = SilkBarcodeService.silkCodeToSilkBarCode(silkCode);
            return map.get(code);
        }).collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private DoffingSpec findDoffingSpec(DoffingType doffingType, Line line, SilkCar silkCar) {
        final List<DoffingSpec> matches = doffingSpecs.parallelStream().filter(it -> {
            if (!Objects.equals(doffingType, it.doffingType)) {
                return false;
            }
            if (!Objects.equals(it.silkCarSpec.type, silkCar.getType())) {
                return false;
            }
            if (!Objects.equals(it.silkCarSpec.row, silkCar.getRow())) {
                return false;
            }
            if (!Objects.equals(it.silkCarSpec.col, silkCar.getCol())) {
                return false;
            }
            return Objects.equals(it.lineName, line.getName());
        }).collect(toList());
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.size() == 0) {
            throw new RuntimeException("没有落筒规则");
        }
        throw new RuntimeException("多种落筒规则");
    }

    @SneakyThrows
    private void watchDoffingSpec() {
        final WatchService watchService = FileSystems.getDefault().newWatchService();
        doffingSpecPath.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        WatchKey key;
        while ((key = watchService.take()) != null) {
            doffingSpecs = fetchDoffingSpec();
            key.reset();
        }
    }

    @SneakyThrows
    private Collection<DoffingSpec> fetchDoffingSpec() {
        final ImmutableList.Builder<DoffingSpec> builder = ImmutableList.builder();
        final String[] extensions = {"yml"};
        for (File file : FileUtils.listFiles(doffingSpecPath.toFile(), extensions, true)) {
            final var spec = YAML_MAPPER.readValue(file, DoffingSpec.class);
            builder.add(spec);
        }
        return builder.build();
    }

    @Data
    public static class DoffingSpec implements Serializable {
        private DoffingType doffingType;
        private String corporationCode;
        private String lineName;
        private SilkCarSpec silkCarSpec;
        private List<SilkCarPositionCheckSpec> silkCarPositionCheckSpecs;
        private List<LineMachineSpec> lineMachineSpecs;
    }

    @Data
    public static class SilkCarSpec implements Serializable {
        private SilkCarType type;
        private int row;
        private int col;
    }

    @Data
    public static class SilkCarPositionCheckSpec implements Serializable, Comparable<SilkCarPositionCheckSpec> {
        private int orderBy;
        private List<SilkCarPosition> silkCarPositions;

        @Override
        public int compareTo(SilkCarPositionCheckSpec o) {
            return ComparisonChain.start()
                    .compare(orderBy, o.orderBy)
                    .result();
        }
    }

    @Data
    public static class LineMachineSpec implements Serializable {
        private int orderBy;
        private int spindleNum;
        private List<LineMachineSilkSpec> lineMachineSilkSpecs;
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class LineMachineSilkSpec extends SilkCarPosition {
        @EqualsAndHashCode.Include
        private int spindle;
    }

}
