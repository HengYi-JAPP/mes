package com.hengyi.japp.mes.auto.report.application.dto;

import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.report.Report;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.Data;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liuyuan
 * @create 2019-06-30 15:44
 * @description
 **/
@Data
public class packagePlanReport implements Serializable {

    private final List<Item> items;

    public packagePlanReport(List<Document> list) {
        final MongoCollection<Document> collection = Report.mongoCollection(Grade.class);
        final List<Document> grades = Flux.from(collection.find()).collectList().block();
        Map<String, List<Document>> groupMap = list.parallelStream().collect(Collectors.groupingBy(silkCarRecord ->
                ((Document) silkCarRecord.get("batch")).getString("batchNo") + silkCarRecord.getString("grade"), Collectors.toList()));
        items = groupMap.keySet()
                .stream()
                .map(key -> {
                    Document batch = (Document) groupMap.get(key).get(0).get("batch");
                    String gradeId = groupMap.get(key).get(0).getString("grade");
                    String batchNo = batch.getString("batchNo");
                    String spec = batch.getString("spec");
                    String tubeColor = batch.getString("tubeColor");
                    String gradeName = grades.stream().filter(it -> it.getString("_id").equals(gradeId)).findFirst().orElse(new Document()).getString("name");
                    Stream<Document> packagingSilkCar = groupMap.get(key).parallelStream().filter(document -> !document.getBoolean("packageFlag", false));
                    Stream<Integer> packagingSilk = groupMap.get(key).parallelStream().filter(document -> !document.getBoolean("packageFlag", false)).map(document -> document.getInteger("silkCount", 0));
                    Stream<Document> packagedSilkCar = groupMap.get(key).parallelStream().filter(document -> document.getBoolean("packageFlag", false));
                    Stream<Integer> packagedSilk = groupMap.get(key).parallelStream().filter(document -> document.getBoolean("packageFlag", false)).map(document -> document.getInteger("silkCount", 0));
                    long packagingSilkCarCount = packagingSilkCar.count();
                    long packagingSilkCount = packagingSilk.reduce((a, b) -> a + b).orElse(0);
                    long packagedSilkCarCount = packagedSilkCar.count();
                    long packagedSilkCount = packagedSilk.reduce((a, b) -> a + b).orElse(0);
                    return new Item(batchNo, spec, tubeColor, gradeName, packagingSilkCarCount, packagingSilkCount, packagedSilkCarCount, packagedSilkCount);
                }).collect(Collectors.toList());
    }

    @Data
    private final class Item {
        private final String batchNo;
        private final String spec;
        private final String tubeColor;
        private final String gradeName;
        private final long packagingSilkCarCount;
        private final long packagingSilkCount;
        private final long packagedSilkCarCount;
        private final long packagedSilkCount;
    }

}
