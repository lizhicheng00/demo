package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import org.junit.jupiter.api.Test;

class NamespaceServiceTest {
    private final NamespaceService namespaceService = new NamespaceService();

    @Test
    void trimsNamespace() {
        assertThat(namespaceService.requireNamespace(" namespace-a ")).isEqualTo("namespace-a");
    }

    @Test
    void rejectsNamespaceLongerThanDatabaseColumn() {
        assertThatThrownBy(() -> namespaceService.requireNamespace("n".repeat(129)))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
    }
}
