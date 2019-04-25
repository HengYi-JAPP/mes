package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.dto.EntityByCodeDTO;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

import java.util.Collection;

/**
 * @author jzb 2018-11-15
 */
@Data
public class BigSilkCarAppendModel {
    @EqualsAndHashCode.Include
    protected final SilkCarRuntime silkCarRuntime;
    protected final int silkCarCapacity;
    protected final float count;

    protected BigSilkCarAppendModel(SilkCarRuntime silkCarRuntime, float count) {
        this.silkCarRuntime = silkCarRuntime;
        this.count = count;
        final SilkCar silkCar = silkCarRuntime.getSilkCarRecord().getSilkCar();
        Validate.isTrue(SilkCarType.BIG_SILK_CAR == silkCar.getType());
        this.silkCarCapacity = silkCar.getRow() * silkCar.getCol() * 2 * 2 - silkCarRuntime.getSilkRuntimes().size();
    }

    public Flowable<SilkRuntime> generateSilkRuntimes(Collection<EntityByCodeDTO> checkSilks) {
        final int lineMachineCount = (int) Math.ceil(count);
        Validate.isTrue(lineMachineCount == checkSilks.size());
        return null;
    }
}
