package com.hengyi.japp.mes.auto.doffing.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author jzb 2019-03-08
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "T_SilkCarRecordHistory")
@NamedQueries({
        @NamedQuery(name = "History.fetchCleanData", query = "select o from AutoDoffingSilkCarRecordAdaptHistory o where o.createDateTime<:cleanTimestamp"),
})
public class AutoDoffingSilkCarRecordAdaptHistory extends AbstractAutoDoffingSilkCarRecordAdapt {
}
