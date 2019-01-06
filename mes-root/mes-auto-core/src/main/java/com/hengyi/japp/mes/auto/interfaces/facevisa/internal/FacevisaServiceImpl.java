package com.hengyi.japp.mes.auto.interfaces.facevisa.internal;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.interfaces.facevisa.FacevisaService;
import com.hengyi.japp.mes.auto.interfaces.facevisa.dto.AutoVisualInspectionSilkInfoDTO;
import com.hengyi.japp.mes.auto.interfaces.jikon.dto.GetSilkSpindleInfoDTO;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hengyi.japp.mes.auto.interfaces.jikon.JikonAdapter.SQL_TPL;
import static com.hengyi.japp.mes.auto.interfaces.jikon.dto.GetSilkSpindleInfoDTO.eliminateFlage_YES;

/**
 * @author jzb 2018-10-20
 */
@Slf4j
@Singleton
public class FacevisaServiceImpl implements FacevisaService {
    private final JDBCClient jikonDS;
    private final SilkRepository silkRepository;

    @Inject
    private FacevisaServiceImpl(@Named("jikonDS") JDBCClient jikonDS, SilkRepository silkRepository) {
        this.jikonDS = jikonDS;
        this.silkRepository = silkRepository;
    }

    @Override
    public Single<AutoVisualInspectionSilkInfoDTO> autoVisualInspection_silkInfo(String code) {
        return silkRepository.findByCode(code).toSingle().map(silk -> {
            final AutoVisualInspectionSilkInfoDTO result = new AutoVisualInspectionSilkInfoDTO();
            final LineMachine lineMachine = silk.getLineMachine();
            final Line line = lineMachine.getLine();
            final Workshop workshop = line.getWorkshop();
            final Batch batch = silk.getBatch();
            result.setSilk_code(silk.getCode());
            result.setWorkshop_id(workshop.getId());
            result.setWorkshop_name(workshop.getName());
            result.setLine_id(line.getId());
            result.setLine_name(line.getName());
            result.setItem(lineMachine.getItem());
            result.setSpindle_no(silk.getSpindle());
            result.setBatch_no(batch.getBatchNo());
            result.setSpec(batch.getSpec());
            result.setProduct_date_time(silk.getDoffingDateTime());
            return result;
        });
    }

    @Override
    public Completable prepare(GetSilkSpindleInfoDTO dto) {
        return Flowable.fromIterable(J.emptyIfNull(dto.getList())).map(item -> {
            final HashMap<String, String> map = Maps.newHashMap();
            final SilkRuntime silkRuntime = item.getSilkRuntime();
            final Silk silk = silkRuntime.getSilk();
            final LineMachine lineMachine = silk.getLineMachine();
            final Line line = lineMachine.getLine();
            final Workshop workshop = line.getWorkshop();
            final Batch batch = silk.getBatch();
            map.put("workshop_name", J.defaultString(workshop.getName()));
            map.put("line_name", J.defaultString(line.getName()));
            map.put("spec", J.defaultString(batch.getSpec()));
            map.put("batch_no", J.defaultString(item.getBatchNo()));
            map.put("item", J.defaultString("" + lineMachine.getItem()));
            map.put("fall_no", J.defaultString(silk.getDoffingNum()));
            map.put("spindle_no", J.defaultString("" + silk.getSpindle()));
            map.put("silk_code", J.defaultString(item.getSpindleCode()));
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            final String product_date = Optional.ofNullable(silk.getDoffingDateTime())
                    .map(simpleDateFormat::format)
                    .orElse("");
            map.put("product_date", product_date);
            if (eliminateFlage_YES.equals(item.getEliminateFlage())) {
                map.put("is_exception", "Y");
                final String s1 = J.emptyIfNull(item.getSilkExceptions()).stream()
                        .map(SilkException::getName)
                        .collect(Collectors.joining(","));
                final String s2 = J.emptyIfNull(item.getDyeingExceptionStrings()).stream()
                        .filter(J::nonBlank)
                        .collect(Collectors.joining(","));
                final String s = String.join(",", s1, s2);
                final String exception_info;
                if (s.length() > 40) {
                    exception_info = s.substring(0, 39);
                } else {
                    exception_info = s;
                }
                map.put("exception_info", exception_info);
            } else {
                map.put("is_exception", "N");
                map.put("exception_info", "");
            }
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            map.put("create_time", df.format(new Date()));
            return J.strTpl(SQL_TPL, map);
        }).toList().flatMapCompletable(sqls -> {
            log.info(sqls.toString());
            return jikonDS.rxGetConnection().flatMap(sqlConnection ->
                    sqlConnection.rxBatch(sqls).doAfterTerminate(sqlConnection::close)
            ).ignoreElement();
        }).doOnError(ex -> log.error("prepareFacevisa", ex));
    }
}
