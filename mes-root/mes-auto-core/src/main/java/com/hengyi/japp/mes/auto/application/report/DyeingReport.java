package com.hengyi.japp.mes.auto.application.report;

import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author liuyuan
 * @create 2019-04-22 10:06
 * @description 织袜报表
 **/
@Data
public class DyeingReport implements Serializable {
    private final Collection<DyeingPrepare> dyeingPrepares;

    public DyeingReport(Collection<DyeingPrepare> dyeingPrepares) {
        this.dyeingPrepares = dyeingPrepares;
    }

}
