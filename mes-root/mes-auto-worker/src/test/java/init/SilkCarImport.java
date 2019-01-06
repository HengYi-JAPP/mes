package init;

import com.hengyi.japp.mes.auto.application.command.SilkCarUpdateCommand;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import io.vertx.core.json.JsonObject;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static init.UserImport.*;

/**
 * @author jzb 2018-11-08
 */
@Slf4j
public class SilkCarImport {

    public static void main(String[] args) throws IOException {
        final Collection<SilkCarUpdateCommand> commands = getTasks("/home/mes/init/silkcar.xlsx");
        final SilkCarUpdateCommand.Batch command = new SilkCarUpdateCommand.Batch();
        command.setCommands(commands);
        RequestBody body = RequestBody.create(JSON, JsonObject.mapFrom(command).encode());
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url("http://10.2.0.215:9998/api/batchSilkCars")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.isSuccessful());
        response.close();
    }

    @SneakyThrows
    private static Collection<SilkCarUpdateCommand> getTasks(String path) {
        @Cleanup final Workbook wb = new XSSFWorkbook(path);
        final Sheet sheet = wb.getSheetAt(0);
        return StreamSupport.stream(sheet.spliterator(), false)
                .skip(1)
                .map(row -> {
                    final SilkCarUpdateCommand command = new SilkCarUpdateCommand();
                    command.setCode(getString(row, 0));
                    command.setNumber(getString(row, 1));
                    final int count = getInt(row, 2) / 2;
                    switch (count) {
                        case 12: {
                            command.setRow(3);
                            command.setCol(4);
                            break;
                        }
                        case 15: {
                            command.setRow(3);
                            command.setCol(5);
                            break;
                        }
                        case 16: {
                            command.setRow(4);
                            command.setCol(4);
                            break;
                        }
                        case 18: {
                            command.setRow(3);
                            command.setCol(6);
                            break;
                        }
                        case 24: {
                            command.setRow(4);
                            command.setCol(6);
                            break;
                        }
                        case 30: {
                            command.setRow(3);
                            command.setCol(5);
                            break;
                        }
                        case 40: {
                            command.setRow(5);
                            command.setCol(8);
                            break;
                        }
                        default: {
                            log.error("丝锭数量无法计算，count=" + count);
                        }
                    }
                    if (getInt(row, 3) == 1) {
                        command.setType(SilkCarType.DEFAULT);
                    } else {
                        command.setType(SilkCarType.BIG_SILK_CAR);
                    }
                    return command;
                })
                .collect(Collectors.toList());
    }


}
