package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.huawei.devbridge.relaycontroller.common.util.Base32Utils;
import org.junit.jupiter.api.Test;

class Base32UtilsTest {
    @Test
    void encode40BitUsesFixedWidthLowercaseBase32() {
        assertThat(Base32Utils.encode40Bit(1L)).isEqualTo("aaaaaaab");
        assertThat(Base32Utils.encode40Bit(123456L)).isEqualTo("aaaadysa");
        assertThat(Base32Utils.encode40Bit((1L << 40) - 1)).isEqualTo("77777777");
    }
}
