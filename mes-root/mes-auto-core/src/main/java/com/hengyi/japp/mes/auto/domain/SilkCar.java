package com.hengyi.japp.mes.auto.domain;

import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.stream.IntStream;

/**
 * 丝车
 *
 * @author jzb 2018-06-20
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class SilkCar extends LoggableMongoEntity {
    private SilkCarType type;
    /**
     * 丝车编号
     */
    private String number;
    /**
     * 丝车编码，条形码
     */
    @ToString.Include
    private String code;
    private int row;
    private int col;
    /**
     * 丝车层数（有的丝车会放两层丝锭，尤其是大丝车）
     */
    private int pliesNum;

    public List<CheckSilkDTO> checkSilks(SilkCarSideType silkCarSideType) {
        final ImmutableList.Builder<CheckSilkDTO> builder = ImmutableList.builder();
        IntStream.rangeClosed(1, getRow()).mapToObj(r -> checkSilks(silkCarSideType, r)).forEach(builder::addAll);
        return builder.build();
    }

    public List<CheckSilkDTO> checkSilks(SilkCarSideType silkCarSideType, int row) {
        final ImmutableList.Builder<CheckSilkDTO> builder = ImmutableList.builder();
        IntStream.rangeClosed(1, getCol()).filter(c -> !isMiddle(c)).mapToObj(c -> {
            final CheckSilkDTO checkSilk = new CheckSilkDTO();
            checkSilk.setSideType(silkCarSideType);
            checkSilk.setRow(row);
            checkSilk.setCol(c);
            return checkSilk;
        }).forEach(builder::add);
        return builder.build();
    }

    public boolean isMiddle(int c) {
        final int col = getCol();
        if (col % 2 == 0) {
            return false;
        }
        final int i = col / 2;
        return c == (i + 1);
    }

}
