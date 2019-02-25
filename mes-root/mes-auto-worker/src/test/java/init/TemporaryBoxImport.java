package init;

import com.hengyi.japp.mes.auto.application.command.TemporaryBoxUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Cleanup;
import lombok.Data;
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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static init.UserImport.*;

/**
 * @author jzb 2018-11-08
 */
@Slf4j
public class TemporaryBoxImport {

    public static void main(String[] args) throws Exception {
        final Map<String, Batch> batchMap = batchMap();
        final Map<String, Grade> gradeMap = gradeMap();
        final Collection<SingleTask> tasks = getTasks("/home/mes/init/temporary.xlsx", batchMap, gradeMap);
        ForkJoinPool.commonPool().invokeAll(tasks);
    }

    private static Map<String, Grade> gradeMap() throws IOException {
        final Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token)
                .url("http://10.2.0.215:9998/api/temporaryBoxes")
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        return null;
    }

    private static Map<String, Batch> batchMap() {
        return null;
    }

    @SneakyThrows
    private static Collection<SingleTask> getTasks(String path, Map<String, Batch> batchMap, Map<String, Grade> gradeMap) {
        @Cleanup final Workbook wb = new XSSFWorkbook(path);
        final Sheet sheet = wb.getSheetAt(0);
        return StreamSupport.stream(sheet.spliterator(), false)
                .skip(1)
                .map(row -> {
                    final String code = getString(row, 0);
                    final Batch batch = batchMap.get(getString(row, 1));
                    final Grade grade = gradeMap.get(getString(row, 2));
                    return new SingleTask(code, batch, grade);
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class SingleTask implements Callable<SingleTask> {
        public final String code;
        public final Batch batch;
        public final Grade grade;
        public boolean notHrId;
        public boolean noHrUserAd;
        public boolean handled;

        public SingleTask(String code, Batch batch, Grade grade) {
            this.code = code;
            this.batch = batch;
            this.grade = grade;
        }

        @Override
        public SingleTask call() throws Exception {
            final TemporaryBoxUpdateCommand command = new TemporaryBoxUpdateCommand();
            command.setCode(code);
            command.setBatch(MAPPER.convertValue(batch, EntityDTO.class));
            command.setGrade(MAPPER.convertValue(grade, EntityDTO.class));

            final RequestBody body = RequestBody.create(JSON, MAPPER.writeValueAsString(command));
            final Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token)
                    .url("http://10.2.0.215:9998/api/temporaryBoxes")
                    .post(body)
                    .build();
            @Cleanup final Response response = client.newCall(request).execute();
            handled = response.isSuccessful();
            return this;
        }
    }

}
