package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.application.SilkCarRuntimeService;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.exception.MultiBatchException;
import com.hengyi.japp.mes.auto.exception.MultiGradeException;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PackageBoxEvent extends EventSource {
    private PackageBox packageBox;
    private JsonNode command;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        final Collection<Silk> silks = J.emptyIfNull(packageBox.getSilks());
        return J.emptyIfNull(data).stream()
                .filter(it -> !silks.contains(it.getSilk()))
                .collect(Collectors.toList());
//        return Lists.newArrayList();
    }

    @Override
    protected Completable _undo(Operator operator) {
        // todo 打包暂时不支持撤销
        throw new IllegalAccessError();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.PackageBoxEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends EventSource.DTO {
        private EntityDTO packageBox;
        private JsonNode command;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<PackageBoxEvent> toEvent() {
            final PackageBoxRepository packageBoxRepository = Jvertx.getProxy(PackageBoxRepository.class);

            final PackageBoxEvent event = new PackageBoxEvent();
            event.setCommand(command);
            return packageBoxRepository.find(packageBox.getId()).flatMap(packageBox -> {
                event.setPackageBox(packageBox);
                return toEvent(event);
            });
        }
    }

    @Data
    public static class ManualCommand implements Serializable {
        @NotNull
        @Size(min = 1)
        private Collection<Item> items;

        public Single<List<Pair<SilkCarRuntime, List<SilkRuntime>>>> checkAndGetData() {
            final SilkCarRuntimeService silkCarRuntimeService = Jvertx.getProxy(SilkCarRuntimeService.class);

            return Flowable.fromIterable(items).flatMapSingle(item -> silkCarRuntimeService.find(item.getSilkCarRecord()).map(silkCarRuntime -> {
                final List<SilkRuntime> silkRuntimes = silkCarRuntime.pickSilks(item.count);
                return Pair.of(silkCarRuntime, silkRuntimes);
            })).toList().map(pairs -> {
                final Set<Batch> batchSet = pairs.stream().map(Pair::getKey).map(SilkCarRuntime::getSilkCarRecord).map(SilkCarRecord::getBatch).collect(Collectors.toSet());
                if (batchSet.size() != 1) {
                    throw new MultiBatchException();
                }

                final Set<Grade> gradeSet = pairs.stream().map(Pair::getKey).map(SilkCarRuntime::getSilkRuntimes).flatMap(Collection::stream).map(SilkRuntime::getGrade).collect(Collectors.toSet());
                if (gradeSet.size() != 1) {
                    throw new MultiGradeException();
                }

                // todo 加强数据管理
//                final Set<Silk> packagedSilkSet = pairs.stream().map(Pair::getKey).map(SilkCarRuntime::getSilkRuntimes).flatMap(Collection::stream).map(SilkRuntime::getSilk).filter(it -> it.getPackageBox() != null).collect(Collectors.toSet());
//                if (packagedSilkSet.size() != 1) {
//                    throw new RuntimeException("丝锭已打包");
//                }
                return pairs;
            });
        }

        @Data
        public static class Item implements Serializable {
            @NotNull
            private SilkCarRecordDTO silkCarRecord;
            @Min(1)
            private int count;
        }
    }

    @Data
    public static class ManualCommandSimple implements Serializable {
        @Min(1)
        private int silkCount;
        @NotNull
        @Size(min = 1)
        private Collection<SilkCarRecordDTO> silkCarRecords;
    }

    @Data
    public static class TemporaryBoxCommand implements Serializable {
        @NotNull
        private EntityDTO temporaryBox;
        @Min(1)
        private int count;
    }

}
