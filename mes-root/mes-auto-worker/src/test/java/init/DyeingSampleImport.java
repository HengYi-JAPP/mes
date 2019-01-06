package init;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.repository.DyeingSampleRepository;
import com.mongodb.client.MongoDatabase;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static init.UserImport.*;

/**
 * @author jzb 2018-11-08
 */
@Slf4j
public class DyeingSampleImport {
    @SneakyThrows
    public static void main(String[] args) {
        final List<StandardXlsxItem> standardXlsx = StandardXlsxItem.load("/home/mes/init/standard.xlsx");
        importUser(standardXlsx).subscribe(() -> System.out.println("===finish==="));
        TimeUnit.DAYS.sleep(1);
    }

    private static Completable importUser(List<StandardXlsxItem> data) {
        final DyeingSampleRepository dyeingSampleRepository = injector.getInstance(DyeingSampleRepository.class);
        return Flowable.fromIterable(Sets.newHashSet(data)).flatMapSingle(item -> dyeingSampleRepository.create().map(dyeingSample -> {
            dyeingSample.setCode(item.getCode());
            dyeingSample.setLineName(item.getLineName());
            dyeingSample.setLineMachineItem(item.getLineMachineItem());
            dyeingSample.setSpindle(item.getSpindle());
            dyeingSample.setBatchNo(item.getBatchNo());
            dyeingSample.setDoffingNum(item.getDoffingNum());
            return dyeingSample;
        })).toList().flatMapCompletable(list -> {
            final MongoDatabase mongoDatabase = injector.getInstance(MongoDatabase.class);
//            mongoDatabase.createCollection("T_DyeingSample");
            final List<Document> documents = Sets.newHashSet(list).stream().map(dyeingSample -> {
                final ObjectNode node = MAPPER.convertValue(dyeingSample, ObjectNode.class);
                node.remove("id");
                node.put("_id", dyeingSample.getCode());
                return Document.parse(node.toString());
            }).collect(Collectors.toList());
            mongoDatabase.getCollection("T_DyeingSample").insertMany(documents);
            return Completable.complete();
        });
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class StandardXlsxItem implements Serializable {
        @EqualsAndHashCode.Include
        private String code;
        private String lineName;
        private int lineMachineItem;
        private int spindle;
        private String batchNo;
        private String doffingNum;

        @SneakyThrows
        private static List<StandardXlsxItem> load(String path) {
            @Cleanup final Workbook wb = new XSSFWorkbook(path);
            final Sheet sheet = wb.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(row -> {
                        final StandardXlsxItem item = new StandardXlsxItem();
                        final String code = getString(row, 0);
                        final String lineName = getString(row, 1);
                        final int lineMachineItem = getInt(row, 2);
                        final int spindle = getInt(row, 3);
                        final String batchNo = getString(row, 4);
                        final String doffingNum = getString(row, 5);
                        item.setCode(J.deleteWhitespace(code));
                        item.setLineName(lineName);
                        item.setLineMachineItem(lineMachineItem);
                        item.setSpindle(spindle);
                        item.setBatchNo(batchNo);
                        item.setDoffingNum(doffingNum);
                        return item;
                    })
                    .collect(Collectors.toList());
        }
    }

}
