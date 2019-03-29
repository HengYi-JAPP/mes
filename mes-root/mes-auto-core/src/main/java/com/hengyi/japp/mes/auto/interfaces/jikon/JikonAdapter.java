package com.hengyi.japp.mes.auto.interfaces.jikon;

import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkCarInfoFetchEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

/**
 * 北自所接口服务
 * <p>
 * 适配老接口
 *
 * @author jzb 2018-06-20
 */
public interface JikonAdapter {
    Logger LOG = LoggerFactory.getLogger(JikonAdapter.class);

    String SQL_TPL = "insert aut_open_silk_info (workshop_name,line_name,spec,batch_no,item,sign,fall_no,spindle_no,silk_code,product_date,is_exception,exception_info,is_confirm,creator,create_time) values ('${workshop_name}','${line_name}','${spec}','${batch_no}','${item}','JAPP-MES','${fall_no}','${spindle_no}','${silk_code}','${product_date}','${is_exception}','${exception_info}','N','JAPP-MES','${create_time}')";

    Single<String> handle(Principal principal, JikonAdapterSilkCarInfoFetchEvent.Command command);

    Single<String> handle(Principal principal, JikonAdapterSilkDetachEvent.Command command);

    Single<String> handle(Principal principal, JikonAdapterPackageBoxEvent.Command command);

}
