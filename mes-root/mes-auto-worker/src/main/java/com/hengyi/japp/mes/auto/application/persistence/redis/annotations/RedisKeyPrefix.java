package com.hengyi.japp.mes.auto.application.persistence.redis.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author jzb 2018-06-24
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface RedisKeyPrefix {
    String value() default "";
}
