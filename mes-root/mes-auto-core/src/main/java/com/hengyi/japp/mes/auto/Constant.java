package com.hengyi.japp.mes.auto;

/**
 * @author jzb 2018-04-13
 */
public class Constant extends com.github.ixtf.japp.core.Constant {
    public static final String IF_SIGN_HEADER = "JAPP_MES_IF_SIGN_HEADER";
    public static final String JWT_ALGORITHM = "RS256";

    public static final class AMQP {
        public static final String MES_AUTO_SILK_PRINT_PREFIX = "Silk";
        public static final String MES_AUTO_SILK_CAR_PRINT_PREFIX = "SilkCar";
        public static final String MES_AUTO_PACKAGE_PRINT_PREFIX = "Package";
        public static final String MES_AUTO_DYEING_RESULT_CREATE_EXCHANGE = "mes.auto.dyeing.result.create.exchange";
        public static final String MES_AUTO_APM_EXCHANGE = "mes.auto.apm.exchange";
        public static final String MES_AUTO_PRINT_EXCHANGE = "mes.auto.print.exchange";
        public static final String MES_AUTO_ES_EXCHANGE = "mes.auto.es.exchange";
    }

    public static final class ErrorCode extends com.github.ixtf.japp.core.Constant.ErrorCode {
        // 落筒车间有误
        public static final String DOFFING_WORKSHOP = "E00004";
        // 丝车容量和落丝颗数不等
        public static final String DOFFING_CAPACITY = "E00005";
        // 丝车混批
        public static final String MULTI_BATCH = "E00006";
        // 丝锭标签
        public static final String DOFFING_TAG = "E00007";
        // 染判结果未出
        public static final String DYEING_NO_RESULT = "E00008";
        // 丝车状态异常
        public static final String SILK_CAR_STATUS = "E00009";
        // 混等级
        public static final String MULTI_GRADE = "E00010";
        // 丝车非空
        public static final String SILKCAR_NON_EMPTY = "E00011";
        // 丝锭重复落筒
        public static final String SILK_DUPLICATE = "E00012";
        // 丝锭重复落筒，提前打印的标签，已经换批
        public static final String BATCH_CHANGED = "E00013";
        // 丝车已打包
        public static final String SILK_CAR_RUNTIME_PACKAGED = "E00014";
    }
}
