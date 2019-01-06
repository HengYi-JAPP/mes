package init;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.worker.WorkerModule;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

/**
 * @author jzb 2018-11-08
 */
@Slf4j
public class UserImport {
    public static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1YjM4NGIyY2Q4NzEyMDY0ZjEwMWUzMWUiLCJ1aWQiOiI1YjM4NGIyY2Q4NzEyMDY0ZjEwMWUzMWUiLCJpc3MiOiJqYXBwLW1lcy1hdXRvIn0.h-CPVnDFw0YyCfm7MIAgXIqTlecAhT5VQe43i5aIUeE";
    public static final Vertx vertx = Vertx.vertx();
    public static final Injector injector = Guice.createInjector(new WorkerModule(vertx));
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
    }

    public static String getString(Row row, int col) {
        final Cell cell = CellUtil.getCell(row, col);
        if (NUMERIC == cell.getCellType()) {
            final Double numericCellValue = cell.getNumericCellValue();
            return String.valueOf(numericCellValue.intValue());
        } else {
            return cell.getStringCellValue();
        }
    }

    public static Double getNumeric(Row row, int col) {
        final Cell cell = CellUtil.getCell(row, col);
        if (NUMERIC == cell.getCellType()) {
            return cell.getNumericCellValue();
        } else {
            return Double.parseDouble(cell.getStringCellValue());
        }
    }

    public static int getInt(Row row, int col) {
        final Cell cell = CellUtil.getCell(row, col);
        if (NUMERIC == cell.getCellType()) {
            final Double numericCellValue = cell.getNumericCellValue();
            return numericCellValue.intValue();
        } else {
            return Integer.parseInt(cell.getStringCellValue());
        }
    }


}
