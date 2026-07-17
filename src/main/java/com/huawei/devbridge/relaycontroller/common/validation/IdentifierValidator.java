package com.huawei.devbridge.relaycontroller.common.validation;

import com.huawei.clouds.wushan.security.redos.TimeoutRegexCharSequence;
import java.util.regex.Pattern;

public final class IdentifierValidator {
    public static final String REGEX = "^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private IdentifierValidator() {
    }

    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(new TimeoutRegexCharSequence(value)).matches();
    }
}
