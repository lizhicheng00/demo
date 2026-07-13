package com.huawei.clouds.wushan.scc.crypto;

import org.springframework.stereotype.Component;

@Component
public class SccCrypto {
    private static final String PREFIX = "{scc}";

    public String encrypt(String value) {
        return value;
    }

    public String decrypt(String value) {
        if (value != null && value.startsWith(PREFIX)) {
            return value.substring(PREFIX.length());
        }
        return value;
    }
}
