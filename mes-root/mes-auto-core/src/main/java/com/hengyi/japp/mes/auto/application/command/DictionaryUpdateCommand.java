package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liuyuan
 * @create 2019-03-14 14:35
 * @description
 **/
@Data
public class DictionaryUpdateCommand implements Serializable {
    private String key;
    private String value;
}
