package com.huawei.devbridge.relaycontroller.common.util;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;

public final class HexUtils {
    private HexUtils() {
    }

    public static String toFixedWidthHex(long value, int minWidth) {
        String hex = Long.toHexString(value);
        if (hex.length() >= minWidth) {
            return hex;
        }
        return "0".repeat(minWidth - hex.length()) + hex;
    }

    public static long parseUnsignedLong(String value, ErrorCode errorCode) {
        try {
            if (StringUtils.isBlank(value)) {
                throw new NumberFormatException("blank hex");
            }
            return Long.parseUnsignedLong(value, 16);
        } catch (NumberFormatException exception) {
            throw new BizException(errorCode);
        }
    }
}
