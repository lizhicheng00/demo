package com.huawei.cloudspace.commons.framework.utils;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static String anonymousMessage(Throwable exception) {
        if (exception == null) {
            return "unknown";
        }
        return rootCause(exception).getClass().getSimpleName();
    }

    private static Throwable rootCause(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
