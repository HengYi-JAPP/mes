package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Collection;

import static java.util.stream.Collectors.joining;

/**
 * @author jzb 2018-07-28
 */
public class DailyReportNotExistException extends JException {
    private final Collection<File> files;

    public DailyReportNotExistException(Collection<File> files) {
        super("");
        this.files = files;
    }

    public DailyReportNotExistException(File file) {
        this(ImmutableList.of(file));
    }

    @Override
    public String getMessage() {
        final String s = files.stream().map(File::getName).collect(joining(","));
        return "[" + s + "]，日报未生成";
    }
}
