package test;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.report.ReportModule;
import io.vertx.reactivex.core.Vertx;

import java.text.DecimalFormat;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;

/**
 * @author jzb 2019-06-04
 */
public class LuceneTest {
    private static final Vertx vertx = Vertx.vertx();

    static {
        // sshfs -o ro root@10.2.0.215:/data/mes/auto/db /data/mes-3000/auto/db
//        System.setProperty("japp.mes.auto.path", "/data/mes-3000/auto");
        // sshfs -o ro root@10.61.0.15:/data/mes/auto/db /data/mes-9200/auto/db
        System.setProperty("japp.mes.auto.path", "/data/mes-9200/auto");
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());
    }

    public static void main(String[] args) {
//        final var service = INJECTOR.getInstance(SilkExceptionReportService.class);
//        final var command = new ExceptionRecordReportCommand();
//        command.setWorkshopId("5c877549a3f0a02467a817f0");
//        command.setStartDateTime(new Date(1575244800000l));
//        command.setEndDateTime(new Date(1575763200000l));
//        final var items = service.report(command).block();
//        System.out.println(items);

        // 纬度
        String lng = "72991799";
        // 度
        int a = Integer.parseInt(lng.substring(0, 2), 16);// 首位,16进制转换为10进制为度
        // 分
        int a1 = Integer.parseInt(lng.substring(2, 4), 16);// 分,第1位
        int a2 = Integer.parseInt(lng.substring(4, 6), 16);// 分,第2位
        int a3 = Integer.parseInt(lng.substring(6, 8), 16);// 分,第3位
        // 将分转换为度
        double a4 = Double.parseDouble("0." + addZero(a1) + addZero(a2) + addZero(a3));
        int a5 = new Double(String.valueOf(a4 * 1000000)).intValue();
        DecimalFormat df = new DecimalFormat("0.00");
        String a6 = df.format((float) a5 / 60);
        String a7 = String.valueOf((int) (new Double(a6) * 100));
        // 补位
        for (int j = 0; j < (6 - a7.length()); j++) {
            a7 = "0" + a7;
        }
        String tempLng = String.valueOf(a) + "." + a7;

        System.out.println(tempLng);

    }

    public static String addZero(int num) {
        String result = "";
        if (num < 10) {
            result = "0" + num;
        } else {
            result = String.valueOf(num);
        }
        return result;
    }
}
