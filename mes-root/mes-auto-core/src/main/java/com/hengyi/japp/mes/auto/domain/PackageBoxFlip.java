package com.hengyi.japp.mes.auto.domain;

import com.hengyi.japp.mes.auto.domain.data.PackageBoxFlipType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * 翻包
 *
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PackageBoxFlip extends LoggableMongoEntity {
    private PackageBoxFlipType type;
    private PackageBox packageBox;
    private Collection<Silk> inSilks;
    private Collection<Silk> outSilks;
}
