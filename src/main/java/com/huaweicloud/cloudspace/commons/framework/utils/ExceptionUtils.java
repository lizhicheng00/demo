package com.huaweicloud.cloudspace.commons.framework.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static String anonymousMessage(Throwable exception) {
        if (exception == null) {
            return "unknown";
        }
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString().stripTrailing();
    }
}
