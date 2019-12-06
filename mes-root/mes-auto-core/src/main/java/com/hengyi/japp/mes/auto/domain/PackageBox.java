package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.domain.data.SaleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.persistence.Entity;
import java.util.Collection;
import java.util.Date;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
public class PackageBox extends LoggableMongoEntity {
    private double tare;
    private double pipeType;
    @ToString.Include
    private PackageBoxType type;
    @ToString.Include
    private String code;
    private SaleType saleType;
    /**
     * 久鼎塑托
     */
    private String palletCode;
    /**
     * 托盘类型
     */
    private String palletType;
    /**
     * 包装类型
     */
    private String packageType;
    /**
     * 泡沫类型
     */
    private String foamType;
    /**
     * 泡沫数量
     */
    private int foamNum;
    @ToString.Include
    private Batch batch;
    @ToString.Include
    private Grade grade;
    private int silkCount;
    private double grossWeight;
    private double netWeight;
    @JsonIgnore
    private Collection<Silk> silks;
    @JsonIgnore
    private Collection<SilkCarRecord> silkCarRecords;
    @JsonIgnore
    private Collection<SilkCarRecord> silkCarRecordsSmall;
    private String smallBatchId;
    private int smallPacageBoxCount;
    private int smallSilkCount;

    @JsonIgnore
    @JsonEntityProperty("command")
    private String commandJsonString;
    private String riambJobId;

    /**
     * 唛头上的打印日期
     */
    private Date printDate;
    private PackageClass printClass;
    private int printCount;
    private String automaticPackeLine;
    /**
     * SAP 入库日期
     */
    private Date budat;
    private PackageClass budatClass;
    private SapT001l sapT001l;
    private boolean inWarehouse;

//    @SneakyThrows
//    @JsonGetter("command")
//    public JsonNode command() {
//        final String commandJsonString = getCommandJsonString();
//        return J.isBlank(commandJsonString) ? null : MAPPER.readTree(commandJsonString);
//    }

    @SneakyThrows
    public void command(JsonNode jsonNode) {
        if (jsonNode != null) {
            setCommandJsonString(MAPPER.writeValueAsString(jsonNode));
        } else {
            setCommandJsonString(null);
        }
    }

    @JsonGetter("creator")
    public Operator _creator_() {
        return getCreator();
    }
}
