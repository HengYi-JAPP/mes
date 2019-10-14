package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-10-12
 */
public class AccReport {
    private static final Collection<String> PRODUCT_PROCESS_IDS = Set.of("5bffac38e189c40001863e47", "5c0084d951e9c40001573d43");
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    private final Map<String, GroupBy_Operator> InspectionMap = Maps.newConcurrentMap();
    private final Map<String, GroupBy_Operator> DoffingMap = Maps.newConcurrentMap();

    public AccReport(String workshopId, long startDateTime, long endDateTime) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public AccReport collect(SilkCarRecordAggregate silkCarRecordAggregate) {
        final String inspectionOperatorId = InspectionOperatorId(silkCarRecordAggregate);
        if (inspectionOperatorId != null) {
            InspectionMap.compute(inspectionOperatorId, (k, v) -> {
                if (v == null) {
                    v = new GroupBy_Operator(inspectionOperatorId);
                }
                return v.collect(silkCarRecordAggregate);
            });
        }
        final String doffingOperatorId = silkCarRecordAggregate.getDoffingOperatorId();
        if (doffingOperatorId != null) {
            DoffingMap.compute(doffingOperatorId, (k, v) -> {
                if (v == null) {
                    v = new GroupBy_Operator(doffingOperatorId);
                }
                return v.collect(silkCarRecordAggregate);
            });
        }
        return this;
    }

    @SneakyThrows
    public void save(String workshopId) {
        final List<JsonNode> doffingList = DoffingMap.values().stream().map(GroupBy_Operator::toJsonNode).collect(toList());
        final File doffingReportDir = FileUtils.getFile("/home/jzb/test/DoffingReport");
        FileUtils.forceMkdir(doffingReportDir);
        YAML_MAPPER.writeValue(FileUtils.getFile(doffingReportDir, workshopId + ".yml"), doffingList);

        final List<JsonNode> inspectionList = InspectionMap.values().stream().map(GroupBy_Operator::toJsonNode).collect(toList());
        final File inspectionReportDir = FileUtils.getFile("/home/jzb/test/InspectionReport");
        FileUtils.forceMkdir(inspectionReportDir);
        YAML_MAPPER.writeValue(FileUtils.getFile(inspectionReportDir, workshopId + ".yml"), inspectionList);
    }

    private String InspectionOperatorId(SilkCarRecordAggregate silkCarRecordAggregate) {
        final Document batch = silkCarRecordAggregate.getBatch();
        if (!Objects.equals(workshopId, batch.getString("workshop"))) {
            return null;
        }
        final long time = silkCarRecordAggregate.getStartDateTime().getTime();
        if (time >= endDateTime) {
            return null;
        }
        final Optional<EventSource.DTO> optional = findEventSourceDTO(silkCarRecordAggregate.getEventSourceDtos());
        if (optional.isPresent()) {
            final EventSource.DTO dto = optional.get();
            final long fireL = dto.getFireDateTime().getTime();
            if (fireL >= this.startDateTime && fireL < endDateTime) {
                return dto.getOperator().getId();
            }
        }
        return null;
    }

    private Optional<EventSource.DTO> findEventSourceDTO(Collection<EventSource.DTO> eventSourceDtos) {
        final List<EventSource.DTO> dtos = eventSourceDtos.parallelStream().filter(it -> {
            if (!it.isDeleted() && EventSourceType.ProductProcessSubmitEvent == it.getType()) {
                final ProductProcessSubmitEvent.DTO dto = (ProductProcessSubmitEvent.DTO) it;
                return PRODUCT_PROCESS_IDS.contains(dto.getProductProcess().getId());
            }
            return false;
        }).collect(toList());
        if (J.isEmpty(dtos)) {
            return Optional.empty();
        }
        Collections.sort(dtos);
        return Optional.of(dtos.get(0));
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Operator {
        @EqualsAndHashCode.Include
        private final Operator operator = new Operator();
        private final Map<String, GroupBy_Batch> batchMap = Maps.newConcurrentMap();

        public GroupBy_Operator(String id) {
            final Document operator = QueryService.find(Operator.class, id).block();
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
        }

        public GroupBy_Operator collect(SilkCarRecordAggregate silkCarRecordAggregate) {
            final Document batch = silkCarRecordAggregate.getBatch();
            batchMap.compute(batch.getString(ID_COL), (k, v) -> {
                if (v == null) {
                    v = new GroupBy_Batch(batch);
                }
                return v.collect(silkCarRecordAggregate);
            });
            return this;
        }

        public JsonNode toJsonNode() {
            final Collection<GroupBy_Product> products = Flux.fromIterable(batchMap.values())
                    .reduce(Maps.<Product, GroupBy_Product>newConcurrentMap(), (acc, cur) -> {
                        final Product product = cur.getBatch().getProduct();
                        acc.compute(product, (k, v) -> {
                            if (v == null) {
                                v = new GroupBy_Product(product);
                            }
                            return v.collect(cur);
                        });
                        return acc;
                    }).map(Map::values).block();
            final Map<String, Object> map = Map.of("operator", this.operator, "products", products);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Batch {
        @EqualsAndHashCode.Include
        private final Batch batch = new Batch();
        private int silkCarRecordCount = 0;
        private int silkCount = 0;

        public GroupBy_Batch(Document batch) {
            this.batch.setId(batch.getString(ID_COL));
            this.batch.setBatchNo(batch.getString("batchNo"));
            this.batch.setSpec(batch.getString("spec"));
            this.batch.setSilkWeight(batch.getDouble("silkWeight"));
            final Product product = new Product();
            this.batch.setProduct(product);
            final Document document = QueryService.findFromCache(Product.class, batch.getString("product")).get();
            product.setId(document.getString(ID_COL));
            product.setName(document.getString("name"));
        }

        public GroupBy_Batch collect(SilkCarRecordAggregate silkCarRecordAggregate) {
            silkCarRecordCount++;
            silkCount += silkCarRecordAggregate.getInitSilkRuntimeDtos().size();
            return this;
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Product {
        @EqualsAndHashCode.Include
        private final Product product;
        private int silkCarRecordCount = 0;
        private int silkCount = 0;

        public GroupBy_Product collect(GroupBy_Batch collect) {
            silkCarRecordCount += collect.silkCarRecordCount;
            silkCount += collect.silkCount;
            return this;
        }
    }

}
