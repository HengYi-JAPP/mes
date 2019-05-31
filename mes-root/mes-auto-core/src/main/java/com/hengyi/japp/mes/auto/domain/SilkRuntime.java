package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.IterableUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2018-07-29
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkRuntime extends SilkCarPosition {
    @ToString.Include
    @EqualsAndHashCode.Include
    private Silk silk;
    private Collection<SilkException> exceptions;
    private Collection<SilkNote> notes;
    private Grade grade;
    @JsonIgnore
    private DyeingResultCalcModel dyeingResultCalcModel = new DyeingResultCalcModel();
    private DyeingResultInfo selfDyeingResultInfo;
    private DyeingResultInfo firstDyeingResultInfo;
    private DyeingResultInfo crossDyeingResultInfo;
    private DyeingResultInfo multiDyeingResultInfo;
//    private DyeingResultInfo finalDyeingResultInfo;

    public void addExceptions(Collection<SilkException> silkExceptions) {
        exceptions = Sets.newHashSet(J.emptyIfNull(exceptions));
        exceptions.addAll(J.emptyIfNull(silkExceptions));
    }

    public void removeException(Collection<SilkException> silkExceptions) {
        exceptions = Sets.newHashSet(J.emptyIfNull(exceptions));
        exceptions.removeAll(J.emptyIfNull(silkExceptions));
    }

    public void addNotes(Collection<SilkNote> silkNotes) {
        notes = Sets.newHashSet(J.emptyIfNull(notes));
        notes.addAll(J.emptyIfNull(silkNotes));
    }

    public void calcDyeing() {
        selfDyeingResultInfo = dyeingResultCalcModel.getSelfDyeingResultInfo(this);
        firstDyeingResultInfo = dyeingResultCalcModel.getFirstDyeingResultInfo();
        crossDyeingResultInfo = dyeingResultCalcModel.getCrossDyeingResultInfo();
    }

    @Data
    public static class DyeingResultCalcModel implements Serializable {
        // 这辆车自身的染判结果
        private Collection<DyeingResult> selfDyeingResults;
        private Collection<DyeingResult> dyeingResults;

        public void add(DyeingResult dyeingResult) {
            if (dyeingResults == null) {
                dyeingResults = Sets.newHashSet(dyeingResult);
            } else {
                dyeingResults.add(dyeingResult);
            }
        }

        public DyeingResultInfo getSelfDyeingResultInfo(SilkRuntime silkRuntime) {
            if (J.isEmpty(selfDyeingResults)) {
                return null;
            }
            final List<DyeingResult> selecteds = selfDyeingResults.parallelStream()
                    .filter(dyeingResult -> Objects.equals(silkRuntime.getSilk(), dyeingResult.getSilk()))
                    .collect(toList());
            if (J.isEmpty(selecteds)) {
                return null;
            }
            final DyeingResultInfo dyeingResultInfo = new DyeingResultInfo();
            final DyeingResult unSubmittedDyeingResult = selecteds.stream().filter(it -> !it.isSubmitted()).findFirst().orElse(null);
            if (unSubmittedDyeingResult != null) {
                dyeingResultInfo.setDyeingResult(unSubmittedDyeingResult);
                return dyeingResultInfo;
            }
            final Grade grade = selecteds.parallelStream().min((o1, o2) -> {
                final Integer sortBy1 = Optional.ofNullable(o1.getGrade()).map(Grade::getSortBy).orElse(0);
                final Integer sortBy2 = Optional.ofNullable(o2.getGrade()).map(Grade::getSortBy).orElse(0);
                return sortBy1 - sortBy2;
            }).map(DyeingResult::getGrade).orElse(null);
            dyeingResultInfo.setGrade(grade);
            final DyeingResult exceptionDyeingResult = selecteds.stream().filter(it -> it.isHasException()).findFirst().orElse(null);
            if (exceptionDyeingResult != null) {
                dyeingResultInfo.setDyeingResult(exceptionDyeingResult);
            } else {
                final DyeingResult dyeingResult = IterableUtils.get(selecteds, 0);
                dyeingResultInfo.setDyeingResult(dyeingResult);
            }
            return dyeingResultInfo;
        }

        public Grade minGrade() {
            final Set<Grade> grades = J.emptyIfNull(dyeingResults).stream()
                    .map(DyeingResult::getGrade)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (J.isEmpty(grades)) {
                return null;
            }
            Grade minGrade = null;
            for (Grade grade : grades) {
                if (minGrade == null) {
                    minGrade = grade;
                } else if (grade.getSortBy() < minGrade.getSortBy()) {
                    minGrade = grade;
                }
            }
            return minGrade;
        }

        public DyeingResultInfo getFirstDyeingResultInfo() {
            return J.emptyIfNull(dyeingResults).stream()
                    .filter(DyeingResult::isFirst)
                    .findFirst()
                    .map(it -> {
                        final DyeingResultInfo dyeingResultInfo = new DyeingResultInfo();
                        dyeingResultInfo.setDyeingResult(it);
                        dyeingResultInfo.setGrade(minGrade());
                        return dyeingResultInfo;
                    })
                    .orElse(null);
        }

        public DyeingResultInfo getCrossDyeingResultInfo() {
            return J.emptyIfNull(dyeingResults).stream()
                    .filter(DyeingResult::isCross)
                    .findFirst()
                    .map(it -> {
                        final DyeingResultInfo dyeingResultInfo = new DyeingResultInfo();
                        dyeingResultInfo.setDyeingResult(it);
                        dyeingResultInfo.setGrade(minGrade());
                        return dyeingResultInfo;
                    })
                    .orElse(null);
        }
    }

    @Data
    public static class DyeingResultInfo implements Serializable {
        private DyeingResult dyeingResult;
        private Grade grade;

        public Grade getGrade() {
            return grade != null ? grade : dyeingResult.getGrade();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends SilkCarPosition {
        @EqualsAndHashCode.Include
        @NotNull
        private EntityDTO silk;

        public SilkRuntime toSilkRuntime() {
            final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);
            final Silk silk = silkRepository.find_(getSilk().getId());
            return toSilkRuntime(silk);
        }

        private SilkRuntime toSilkRuntime(Silk silk) {
            final SilkRuntime silkRuntime = new SilkRuntime();
            silkRuntime.setSilk(silk);
            silkRuntime.setSideType(sideType);
            silkRuntime.setRow(row);
            silkRuntime.setCol(col);
            return silkRuntime;
        }

        public Single<SilkRuntime> rxToSilkRuntime() {
            final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);
            return silkRepository.find(silk.getId()).map(this::toSilkRuntime);
        }
    }
}
