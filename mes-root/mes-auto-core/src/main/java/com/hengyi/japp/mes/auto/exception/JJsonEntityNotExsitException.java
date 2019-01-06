package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;

/**
 * @author jzb 2018-08-05
 */
public class JJsonEntityNotExsitException extends JException {
    private final Class entityClass;
    private final String id;

    public <T extends JsonEntity> JJsonEntityNotExsitException(Class<T> entityClass, String id) {
        super(null);
        this.entityClass = entityClass;
        this.id = id;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public String getId() {
        return id;
    }
}
