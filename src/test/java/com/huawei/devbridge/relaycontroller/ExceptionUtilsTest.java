package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.huawei.devbridge.relaycontroller.common.util.ExceptionUtils;
import org.junit.jupiter.api.Test;

class ExceptionUtilsTest {

    @Test
    void shouldReturnRootCauseTypeOnly() {
        RuntimeException exception = new RuntimeException("token=secret",
                new IllegalArgumentException("password=secret"));

        assertThat(ExceptionUtils.anonymousMessage(exception)).isEqualTo("IllegalArgumentException");
    }
}
