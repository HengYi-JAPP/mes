package com.hengyi.japp.mes.auto.print;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Slf4j
public class AutoPrintDebug {

    public static void start(String[] args) {
        try {

            Print Print = new Print();
            Method method = Print.class.getMethod("main", String[].class);
            method.invoke(Print, (Object) new String[]{});

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public static void stop(String[] args) {
        log.debug("stop called");
        System.exit(0);
    }
}