package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;

/**
 * 统计报表
 *
 * @author jzb 2019-05-29
 */
@Data
public abstract class AbstractStatisticReport implements Serializable {
    protected WorkshopDTO workshop;
    protected int packageBoxCount;
    protected int silkCount;
    protected BigDecimal silkWeight;
    protected Collection<Item> items;
    protected Collection<PackageBoxDTO> unDiffPackageBoxes;
    protected Collection<Item> customDiffItems;

    @SneakyThrows
    public byte[] toByteArray() {
        @Cleanup final Workbook wb = new XSSFWorkbook();
        PoiUtil.fillSheet1(wb.createSheet(), this);
        @Cleanup final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        return baos.toByteArray();
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class Item implements Serializable {
        @Getter
        @EqualsAndHashCode.Include
        private boolean bigSilkCar;
        @EqualsAndHashCode.Include
        private LineDTO line;
        @EqualsAndHashCode.Include
        private BatchDTO batch;
        @EqualsAndHashCode.Include
        private GradeDTO grade;
        private int silkCount;
        private BigDecimal silkWeight;

        public Item(boolean bigSilkCar, LineDTO line, BatchDTO batch, GradeDTO grade) {
            this.bigSilkCar = bigSilkCar;
            this.line = line;
            this.batch = batch;
            this.grade = grade;
        }
    }

    // fixme remove in 3.0
//    @Deprecated
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class SilkDTO extends EntityDTO {
        private LineMachineDTO lineMachine;
        private BatchDTO batch;
        private GradeDTO grade;

        public static Mono<SilkDTO> fetch(String id) {
            return QueryService.find(Silk.class, id).map(document -> {
                final SilkDTO dto = new SilkDTO();
                dto.setId(document.getString(ID_COL));
                dto.setBatch(BatchDTO.findFromCache(document.getString("batch")).get());
                dto.setGrade(GradeDTO.findFromCache(document.getString("grade")).orElse(null));
                dto.setLineMachine(LineMachineDTO.findFromCache(document.getString("lineMachine")).get());
                return dto;
            });
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class PackageBoxDTO extends EntityDTO {
        private PackageBoxType type;
        private String code;
        private BatchDTO batch;
        private GradeDTO grade;
        private int silkCount;
        private BigDecimal netWeight;
        @JsonIgnore
        private Collection<SilkDTO> silks;
        @JsonIgnore
        private Collection<String> silkIds;
        @JsonIgnore
        private Collection<String> silkCarRecordIds;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class WorkshopDTO extends EntityDTO {
        private String code;
        private String name;

        public static Optional<WorkshopDTO> findFromCache(String id) {
            return QueryService.findFromCache(Workshop.class, id).map(document -> {
                final WorkshopDTO dto = new WorkshopDTO();
                dto.setId(document.getString(ID_COL));
                dto.setCode(document.getString("code"));
                dto.setName(document.getString("name"));
                return dto;
            });
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class LineDTO extends EntityDTO {
        private String name;

        public static Optional<LineDTO> findFromCache(String id) {
            return QueryService.findFromCache(Line.class, id).map(document -> {
                final LineDTO dto = new LineDTO();
                dto.setId(document.getString(ID_COL));
                dto.setName(document.getString("name"));
                return dto;
            });
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class LineMachineDTO extends EntityDTO {
        private LineDTO line;

        public static Optional<LineMachineDTO> findFromCache(String id) {
            return QueryService.findFromCache(LineMachine.class, id).map(document -> {
                final LineMachineDTO dto = new LineMachineDTO();
                dto.setId(document.getString(ID_COL));
                dto.setLine(LineDTO.findFromCache(document.getString("line")).get());
                return dto;
            });
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class ProductDTO extends EntityDTO {
        private String name;

        public static Optional<ProductDTO> findFromCache(String id) {
            return QueryService.findFromCache(Product.class, id).map(document -> {
                final ProductDTO dto = new ProductDTO();
                dto.setId(document.getString(ID_COL));
                dto.setName(document.getString("name"));
                return dto;
            });
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class BatchDTO extends EntityDTO implements Comparable<BatchDTO> {
        private String batchNo;
        private String spec;
        private BigDecimal silkWeight;
        private ProductDTO product;

        public static Optional<BatchDTO> findFromCache(String id) {
            return QueryService.findFromCache(Batch.class, id).map(document -> {
                final BatchDTO dto = new BatchDTO();
                dto.setId(document.getString(ID_COL));
                dto.setBatchNo(document.getString("batchNo"));
                dto.setSpec(document.getString("spec"));
                dto.setSilkWeight(BigDecimal.valueOf(document.getDouble("silkWeight")));
                dto.setProduct(ProductDTO.findFromCache(document.getString("product")).get());
                return dto;
            });
        }

        @Override
        public int compareTo(BatchDTO o) {
            return ComparisonChain.start()
                    .compare(batchNo, o.batchNo)
                    .result();
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class GradeDTO extends EntityDTO {
        private String name;
        private String code;
        private int sortBy;

        public static Optional<GradeDTO> findFromCache(String id) {
            return QueryService.findFromCache(Grade.class, id).map(document -> {
                final GradeDTO dto = new GradeDTO();
                dto.setId(document.getString(ID_COL));
                dto.setCode(document.getString("code"));
                dto.setName(document.getString("name"));
                dto.setSortBy(document.getInteger("sortBy"));
                return dto;
            });
        }
    }

}