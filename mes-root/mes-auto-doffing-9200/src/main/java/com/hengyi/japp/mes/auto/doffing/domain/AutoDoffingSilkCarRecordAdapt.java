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
@Table(name = "T_SilkCarRecord")
@NamedQueries({
        @NamedQuery(name = "Current.fetchData", query = "select o from AutoDoffingSilkCarRecordAdapt o")
})
public class AutoDoffingSilkCarRecordAdapt extends AbstractAutoDoffingSilkCarRecordAdapt {
}
