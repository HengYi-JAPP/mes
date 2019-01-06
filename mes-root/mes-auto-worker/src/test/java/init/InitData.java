package init;

import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static init.UserImport.*;

/**
 * @author jzb 2018-11-20
 */
@Slf4j
public class InitData {

    @SneakyThrows
    public static void main(String[] args) {
        final List<ExceptionXlsxItem> exceptionXlsx = ExceptionXlsxItem.load("/home/mes/init/异常.xlsx");
        final List<GradeXlsxItem> gradeXlsx = GradeXlsxItem.load("/home/mes/init/等级.xlsx");
        final List<BatchXlsxItem> batchXlsx = BatchXlsxItem.load("/home/mes/init/批号.xlsx");
        final List<PlanXlsxItem> planXlsx = PlanXlsxItem.load("/home/mes/init/生产计划.xlsx");
        final List<SilkCarXlsxItem> silkCarXlsx = SilkCarXlsxItem.load("/home/mes/init/丝车.xlsx");

        final Completable completable1 = importException(exceptionXlsx);
        final Completable completable2 = importGrade(gradeXlsx);
//        final Completable completable3 = importSilkCar(silkCarXlsx);

        final Single<Map<String, Workshop>> workshopMap$ = importWorkshop(batchXlsx).cache();
        final Single<Map<String, Product>> productMap$ = importProduct(batchXlsx).cache();
        final Single<Map<String, Batch>> batchMap$ = importBatch(batchXlsx, workshopMap$, productMap$).cache();
        final Single<Map<String, Line>> lineMap$ = importLine(planXlsx, workshopMap$).cache();
        final Single<Map<String, LineMachine>> lineMachineMap$ = importLineMachine(planXlsx, lineMap$, batchMap$).cache();

        Completable.mergeArray(completable1, completable2, workshopMap$.ignoreElement(), productMap$.ignoreElement(), batchMap$.ignoreElement(), lineMap$.ignoreElement(), lineMachineMap$.ignoreElement())
                .subscribe(() -> {
                    System.out.println("===finish===");
                });

        TimeUnit.DAYS.sleep(1);
    }

    private static Completable importException(List<ExceptionXlsxItem> data) {
        final SilkExceptionRepository silkExceptionRepository = injector.getInstance(SilkExceptionRepository.class);
        return Flowable.fromIterable(data).flatMapSingle(it -> silkExceptionRepository.create().flatMap(silkException -> {
            silkException.setName(it.name);
            return silkExceptionRepository.save(silkException);
        })).toList().ignoreElement();
    }

    private static Completable importGrade(List<GradeXlsxItem> data) {
        final GradeRepository gradeRepository = injector.getInstance(GradeRepository.class);
        return Flowable.fromIterable(data).flatMapSingle(it -> gradeRepository.create().flatMap(grade -> {
            grade.setId(it.id);
            grade.setName(it.name);
            grade.setCode(it.code);
            return gradeRepository.save(grade);
        })).toList().ignoreElement();
    }

    private static Completable importSilkCar(List<SilkCarXlsxItem> data) {
        final SilkCarRepository silkCarRepository = injector.getInstance(SilkCarRepository.class);
        return Flowable.fromIterable(data).flatMapCompletable(it -> silkCarRepository.create().flatMapCompletable(silkCar -> {
            silkCar.setCode(it.code);
            silkCar.setNumber(it.number);
            silkCar.setType(it.silkCarType());
            final Pair<Integer, Integer> rowCol = it.rowCol();
            silkCar.setRow(rowCol.getLeft());
            silkCar.setCol(rowCol.getRight());
            return silkCarRepository.save(silkCar).ignoreElement();
        }));
    }

    private static Single<Map<String, Workshop>> importWorkshop(List<BatchXlsxItem> data) {
        final CorporationRepository corporationRepository = injector.getInstance(CorporationRepository.class);
        final WorkshopRepository workshopRepository = injector.getInstance(WorkshopRepository.class);
        final Single<Corporation> corporationSingle = corporationRepository.list().toList().map(it -> it.get(0));
        final Flowable<String> stringFlowable = Flowable.fromIterable(data).map(BatchXlsxItem::getWorkshop).distinct();
        return corporationSingle.flatMapPublisher(corporation -> stringFlowable.flatMapSingle(it -> workshopRepository.create().flatMap(workshop -> {
            workshop.setCorporation(corporation);
            workshop.setName(it);
            workshop.setCode(it);
            return workshopRepository.save(workshop);
        }))).toMap(Workshop::getName);
    }

    private static Single<Map<String, Product>> importProduct(List<BatchXlsxItem> data) {
        final ProductRepository productRepository = injector.getInstance(ProductRepository.class);
        return Flowable.fromIterable(data).map(BatchXlsxItem::getProduct).distinct().flatMapSingle(it -> productRepository.create().flatMap(product -> {
            product.setName("00");
            product.setName(it);
            return productRepository.save(product);
        })).toMap(Product::getName);
    }

    private static Single<Map<String, Batch>> importBatch(List<BatchXlsxItem> data, Single<Map<String, Workshop>> workshopMap$, Single<Map<String, Product>> productMap$) {
        final BatchRepository batchRepository = injector.getInstance(BatchRepository.class);
        return workshopMap$.flatMapPublisher(workshopMap -> productMap$.flatMapPublisher(productMap -> Flowable.fromIterable(data).flatMapSingle(it -> batchRepository.create().flatMap(batch -> {
            batch.setBatchNo(it.getBatchNo());
            batch.setCentralValue(it.getCentralValue());
            batch.setHoleNum(it.getHoleNum());
            batch.setSilkWeight(it.getSilkWeight());
            batch.setSpec(it.getSpec());
            batch.setTubeColor(it.getTubeColor());
            batch.setWorkshop(workshopMap.get(it.getWorkshop()));
            batch.setProduct(productMap.get(it.getProduct()));
            return batchRepository.save(batch);
        })))).toMap(Batch::getBatchNo);
    }

    private static Single<Map<String, Line>> importLine(List<PlanXlsxItem> data, Single<Map<String, Workshop>> workshopMap$) {
        final LineRepository lineRepository = injector.getInstance(LineRepository.class);

        return workshopMap$.flatMapPublisher(workshopMap -> Flowable.fromIterable(data).map(PlanXlsxItem::getLine).distinct().flatMapSingle(it -> lineRepository.create().flatMap(line -> {
            line.setDoffingType(DoffingType.MANUAL);
            line.setName(it);
            line.setWorkshop(workshopMap.get("B"));
            return lineRepository.save(line);
        }))).toMap(Line::getName);
    }

    private static Single<Map<String, LineMachine>> importLineMachine(List<PlanXlsxItem> data, Single<Map<String, Line>> lineMap$, Single<Map<String, Batch>> batchMap$) {
        final LineMachineRepository lineMachineRepository = injector.getInstance(LineMachineRepository.class);
        final LineMachineProductPlanRepository lineMachineProductPlanRepository = injector.getInstance(LineMachineProductPlanRepository.class);

        return lineMap$.flatMapPublisher(lineMap -> batchMap$.flatMapPublisher(batchMap -> Flowable.fromIterable(data).flatMapSingle(it -> lineMachineRepository.create().flatMap(lineMachine -> {
            lineMachine.setLine(lineMap.get(it.getLine()));
            lineMachine.setItem(it.getLineMachineItem());
            final List<Integer> spindleSeq = it.getSpindleSeq();
            lineMachine.setSpindleSeq(spindleSeq);
            lineMachine.setSpindleNum(spindleSeq.size());

            return lineMachineProductPlanRepository.create().flatMap(lineMachineProductPlan -> {
                lineMachineProductPlan.setLineMachine(lineMachine);
                lineMachineProductPlan.setBatch(batchMap.get(it.getBatchNo()));
                lineMachineProductPlan.setStartDate(new Date());
                return lineMachineProductPlanRepository.save(lineMachineProductPlan);
            }).flatMap(lineMachineProductPlan -> {
                lineMachine.setProductPlan(lineMachineProductPlan);
                return lineMachineRepository.save(lineMachine);
            });
        })))).toMap(LineMachine::getId);
    }

    @Data
    public static class ExceptionXlsxItem {
        private String name;

        @SneakyThrows
        private static List<ExceptionXlsxItem> load(String path) {
            @Cleanup final Workbook wb = new XSSFWorkbook(path);
            final Sheet sheet = wb.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .map(row -> {
                        final ExceptionXlsxItem item = new ExceptionXlsxItem();
                        final String name = getString(row, 0);
                        item.setName(name);
                        return item;
                    })
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class GradeXlsxItem {
        private String id;
        private String name;
        private String code;

        @SneakyThrows
        private static List<GradeXlsxItem> load(String path) {
            @Cleanup final Workbook wb = new XSSFWorkbook(path);
            final Sheet sheet = wb.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(row -> {
                        final GradeXlsxItem item = new GradeXlsxItem();
                        final String id = getString(row, 0);
                        final String name = getString(row, 1);
                        final String code = getString(row, 2);
                        item.setId(id);
                        item.setName(name);
                        item.setCode(code);
                        return item;
                    })
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class BatchXlsxItem {
        private String workshop;
        private String product;
        private String batchNo;
        private double silkWeight;
        private int centralValue;
        private int holeNum;
        private String tubeColor;

        @SneakyThrows
        private static List<BatchXlsxItem> load(String path) {
            @Cleanup final Workbook wb = new XSSFWorkbook(path);
            final Sheet sheet = wb.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(row -> {
                        final BatchXlsxItem item = new BatchXlsxItem();
                        final String workshop = getString(row, 0);
                        item.setWorkshop(workshop);
                        final String product = getString(row, 1);
                        item.setProduct(product);
                        final String batchNo = getString(row, 2);
                        item.setBatchNo(batchNo);
                        final double silkWeight = getNumeric(row, 3);
                        item.setSilkWeight(silkWeight);
                        final String tubeColor = getString(row, 4);
                        item.setTubeColor(tubeColor);
                        item.setTubeColor(tubeColor);
                        final int centralValue = getInt(row, 5);
                        item.setCentralValue(centralValue);
                        final int holeNum = getInt(row, 6);
                        item.setHoleNum(holeNum);
                        return item;
                    })
                    .collect(Collectors.toList());
        }

        public String getSpec() {
            return centralValue + "dtex/" + holeNum + "f";
        }
    }

    @Data
    public static class PlanXlsxItem {
        private String line;
        private int lineMachineItem;
        private String spindleSeqString;
        private String batchNo;

        @SneakyThrows
        private static List<PlanXlsxItem> load(String path) {
            @Cleanup final Workbook wb = new XSSFWorkbook(path);
            final Sheet sheet = wb.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(row -> {
                        final PlanXlsxItem item = new PlanXlsxItem();
                        final String line = getString(row, 0);
                        item.setLine(line);
                        final int lineMachineItem = getInt(row, 1);
                        item.setLineMachineItem(lineMachineItem);
                        final String spindleSeq = getString(row, 2);
                        item.setSpindleSeqString(spindleSeq);
                        final String batchNo = getString(row, 3);
                        item.setBatchNo(batchNo);
                        return item;
                    })
                    .collect(Collectors.toList());
        }

        public List<Integer> getSpindleSeq() {
            final String[] split = spindleSeqString.split(",");
            return Arrays.stream(split).map(Integer::parseInt).collect(Collectors.toList());
        }
    }

    @Data
    public static class SilkCarXlsxItem {
        private String code;
        private String number;
        private int spec;
        private int type;

        @SneakyThrows
        private static List<SilkCarXlsxItem> load(String path) {
            @Cleanup final Workbook wb = new XSSFWorkbook(path);
            final Sheet sheet = wb.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(row -> {
                        final SilkCarXlsxItem item = new SilkCarXlsxItem();
                        final String code = getString(row, 0);
                        final String number = getString(row, 1);
                        final int spec = getInt(row, 2);
                        final int type = getInt(row, 3);
                        item.setCode(code);
                        item.setNumber(number);
                        item.setSpec(spec);
                        item.setType(type);
                        return item;
                    })
                    .collect(Collectors.toList());
        }

        public Pair<Integer, Integer> rowCol() {
            switch (spec / 2) {
                case 12: {
                    return Pair.of(3, 4);
                }
                case 15: {
                    return Pair.of(3, 5);
                }
                case 16: {
                    return Pair.of(4, 4);
                }
                case 18: {
                    return Pair.of(3, 6);
                }
                case 24: {
                    return Pair.of(4, 6);
                }
                case 30: {
                    return Pair.of(3, 5);
                }
                case 40: {
                    return Pair.of(5, 8);
                }
                default: {
                    log.error("丝锭数量无法计算，spec=" + spec);
                    return Pair.of(0, 0);
                }
            }
        }

        public SilkCarType silkCarType() {
            return type == 1 ? SilkCarType.DEFAULT : SilkCarType.BIG_SILK_CAR;
        }
    }
}
