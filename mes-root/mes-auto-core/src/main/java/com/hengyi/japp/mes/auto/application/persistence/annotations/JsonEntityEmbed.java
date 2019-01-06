package com.hengyi.japp.mes.auto.application.persistence.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author jzb 2018-06-24
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface JsonEntityEmbed {
    String value() default "";

    JsonEntityProperty[] properties();
}
