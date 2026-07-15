package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.huawei.devbridge.relaycontroller.application.service.LocalClusterService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.repository.ClusterRepository;
import com.huawei.devbridge.relaycontroller.domain.service.NamespaceService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import org.junit.jupiter.api.Test;

class InputBoundaryTest {

    @Test
    void shouldNormalizeValidNamespace() {
        assertThat(new NamespaceService().requireNamespace(" namespace-a ")).isEqualTo("namespace-a");
    }

    @Test
    void shouldRejectOversizedNamespace() {
        assertThatThrownBy(() -> new NamespaceService().requireNamespace("a".repeat(129)))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
    }

    @Test
    void shouldRejectInvalidClusterBeforeDatabaseLookup() {
        ClusterRepository repository = mock(ClusterRepository.class);
        LocalClusterService service = new LocalClusterService(repository, new RelayProperties());

        assertThatThrownBy(() -> service.requireLocalCluster("a".repeat(129)))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectClusterWithSurroundingWhitespace() {
        ClusterRepository repository = mock(ClusterRepository.class);
        LocalClusterService service = new LocalClusterService(repository, new RelayProperties());

        assertThatThrownBy(() -> service.requireLocalCluster(" cluster-a "))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARAM_INVALID);
        verifyNoInteractions(repository);
    }
}
