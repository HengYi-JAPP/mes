package com.hengyi.japp.mes.auto.interfaces.riamb.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.DyeingResult;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.hengyi.japp.mes.auto.Util.getDyeingExceptionString;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2018-06-22
 */
@Data
public class RiambFetchSilkCarRecordResultDTO implements Serializable {
    public static final String packeFlage_YES = "2";//可以下自动包装线
    public static final String packeFlage_NO = "1";//不能下自动包装线

    public static final String grabFlage_YES = "1";//抓取
    public static final String grabFlage_NO = "2";//不抓取

    public static final String eliminateFlage_YES = "2";//剔除
    public static final String eliminateFlage_NO = "1";//不剔除
    /**
     * 是否可以下自动包装线（1:不可以，2:可以）
     */
    private String packeFlage;
    private SilkCarInfo silkCarInfo;
    /**
     * 丝锭数量
     */
    private int silkCount;
    private List<SilkInfo> silkInfos;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SilkCarInfo extends EntityDTO {
        private String code;
        private int row;
        private int col;
        private String batchNo;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SilkInfo extends CheckSilkDTO {
        @JsonIgnore
        private Collection<SilkException> silkExceptions;
        @JsonIgnore
        private Set<String> dyeingExceptionStrings;
        @JsonIgnore
        private boolean dyeingSubmitted;
        /**
         * 抓取标识(1:抓取，2:不抓取)
         */
        private String grabFlage;
        /**
         * 剔除标识(1:不剔除，2:剔除)
         */
        private String eliminateFlage;

        private SilkCarSideType sideType;
        private int row;
        private int col;
        /**
         * 纺位名称，A1-19/3
         */
        private String spec;
        /**
         * 批号
         */
        private String batchNo;
        /**
         * 等级
         */
        private String gradeName;
        private String doffingNum;
        private String doffingOperatorName;
        private DoffingType doffingType;
        private Date doffingDateTime;
        private String otherInfo;

        @JsonSetter
        public Collection<String> getExceptions() {
            final var stream1 = J.emptyIfNull(silkExceptions).parallelStream().map(SilkException::getName);
            final var stream2 = J.emptyIfNull(dyeingExceptionStrings).parallelStream();
            return Stream.concat(stream1, stream2).collect(toSet());
        }

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
