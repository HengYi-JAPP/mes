package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.config.DoffingSpec;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.exception.DoffingTagException;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.validation.constraints.NotBlank;
import java.io.File;
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
        return doffingSpec.getSilkCarPositionCheckSpecs().stream().map(it -> {
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
                    silkBarcodes = sort(silkBarcodes, checkSilks);
                    List<DoffingSpec.LineMachineSpec> lineMachineSpecsChecked = null;
                    for (List<DoffingSpec.LineMachineSpec> lineMachineSpecs : doffingSpec.getLineMachineSpecsList()) {
                        final boolean b = checkPositions(checkSilks, lineMachineSpecs, silkBarcodes);
                        if (b) {
                            return generateSilkRuntimes(silkBarcodes, lineMachineSpecs);
                        }
                    }
                    throw new DoffingTagException();
                });
    }

    private Flowable<SilkRuntime> generateSilkRuntimes(List<SilkBarcode> silkBarcodes, List<DoffingSpec.LineMachineSpec> lineMachineSpecsChecked) {
        final List<Single<SilkRuntime>> silkRuntimeList$ = Lists.newArrayList();
        for (int i = 0; i < silkBarcodes.size(); i++) {
            final SilkBarcode silkBarcode = silkBarcodes.get(i);
            final LineMachine lineMachine = silkBarcode.getLineMachine();
            final Batch batch = silkBarcode.getBatch();
            final var lineMachineSpec = lineMachineSpecsChecked.get(i);
            for (var silkSpec : lineMachineSpec.getLineMachineSilkSpecs()) {
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
    }

    private boolean checkPositions(List<CheckSilkDTO> checkSilks, List<DoffingSpec.LineMachineSpec> lineMachineSpecs, List<SilkBarcode> silkBarcodes) {
        for (int i = 0; i < checkSilks.size(); i++) {
            final CheckSilkDTO checkSilk = checkSilks.get(i);
            final SilkBarcode silkBarcode = silkBarcodes.get(i);
            final boolean b = checkPosition(checkSilk, lineMachineSpecs, silkBarcode);
            if (!b) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPosition(CheckSilkDTO checkSilk, List<DoffingSpec.LineMachineSpec> lineMachineSpecs, SilkBarcode silkBarcode) {
        final var silkSpecs = lineMachineSpecs.parallelStream()
                .flatMap(it -> it.getLineMachineSilkSpecs().parallelStream())
                .filter(lineMachineSilkSpec ->
                        Objects.equals(checkSilk.getSideType(), lineMachineSilkSpec.getSideType()) &&
                                Objects.equals(checkSilk.getRow(), lineMachineSilkSpec.getRow()) &&
                                Objects.equals(checkSilk.getCol(), lineMachineSilkSpec.getCol())
                )
                .collect(toList());
        if (silkSpecs.size() == 1) {
            final var silkSpec = silkSpecs.get(0);
            final String code = silkBarcode.generateSilkCode(silkSpec.getSpindle());
            return Objects.equals(code, checkSilk.getCode());
        }
        return false;
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
            if (!Objects.equals(doffingType, it.getDoffingType())) {
                return false;
            }
            if (!Objects.equals(it.getSilkCarSpec().getType(), silkCar.getType())) {
                return false;
            }
            if (!Objects.equals(it.getSilkCarSpec().getRow(), silkCar.getRow())) {
                return false;
            }
            if (!Objects.equals(it.getSilkCarSpec().getCol(), silkCar.getCol())) {
                return false;
            }
            return Objects.equals(it.getLineName(), line.getName());
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

}
