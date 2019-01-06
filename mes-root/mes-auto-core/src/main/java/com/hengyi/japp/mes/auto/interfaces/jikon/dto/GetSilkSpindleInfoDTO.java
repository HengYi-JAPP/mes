package com.hengyi.japp.mes.auto.interfaces.jikon.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.DyeingResult;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.hengyi.japp.mes.auto.Util.getDyeingExceptionString;

/**
 * @author jzb 2018-11-07
 */
@Data
public class GetSilkSpindleInfoDTO implements Serializable {
    public static final String AutomaticPackeFlage_YES = "2";//可以下自动包装线
    public static final String AutomaticPackeFlage_NO = "1";//不能下自动包装线

    public static final String grabFlage_YES = "1";//抓取
    public static final String grabFlage_NO = "2";//不抓取

    public static final String eliminateFlage_YES = "2";//剔除
    public static final String eliminateFlage_NO = "1";//不剔除

    /**
     * 丝锭数量
     */
    private String bindNum;
    /**
     * 丝车规格
     */
    private String spec;
    /**
     * 是否可以下自动包装线（1:不可以，2:可以）
     */
    @JsonProperty("AutomaticPackeFlage")
    private String AutomaticPackeFlage;
    private List<Item> list;

    /**
     * 注：只有grabFlag为抓取和eliminateFlage为不剔除的时候，丝锭才可以被抓取打包"
     */
    @Data
    public static class Item {
        /**
         * 丝锭编码
         */
        private String spindleCode;
        /**
         * 抓取标识(1:抓取，2:不抓取)
         */
        private String grabFlage;
        /**
         * 剔除标识(1:不剔除，2:剔除)
         */
        private String eliminateFlage;
        /**
         * 批号
         */
        private String batchNo;
        /**
         * 锭位号
         */
        private String actualPosition;
        /**
         * 等级
         */
        private String grade;

        @JsonIgnore
        private SilkRuntime silkRuntime;
        @JsonIgnore
        private Collection<SilkException> silkExceptions;
        @JsonIgnore
        private boolean dyeingSubmitted;
        @JsonIgnore
        private Set<String> dyeingExceptionStrings;


        public void addDyeingExceptionString(String dyeingExceptionString) {
            if (J.nonBlank(dyeingExceptionString)) {
                if (dyeingExceptionStrings == null) {
                    dyeingExceptionStrings = Sets.newHashSet(dyeingExceptionString);
                } else {
                    dyeingExceptionStrings.add(dyeingExceptionString);
                }
            }
        }

        public void accept(SilkRuntime.DyeingResultInfo dyeingResultInfo) {
            if (dyeingResultInfo != null) {
                final DyeingResult dyeingResult = dyeingResultInfo.getDyeingResult();
                addDyeingExceptionString(getDyeingExceptionString(dyeingResult));
                if (!dyeingResult.isSubmitted()) {
                    setDyeingSubmitted(false);
                }
            }
        }
    }
}
