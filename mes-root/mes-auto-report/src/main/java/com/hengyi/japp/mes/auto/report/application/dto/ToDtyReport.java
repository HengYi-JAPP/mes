package com.hengyi.japp.mes.auto.report.application.dto;

import lombok.Data;
import org.bson.Document;

import java.io.Serializable;
import java.util.List;

/**
 * @author jzb 2019-07-11
 */
@Data
public class ToDtyReport implements Serializable {
    public ToDtyReport(List<Document> documents) {
    }
}
