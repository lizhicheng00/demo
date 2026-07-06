package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.huawei.devbridge.relaycontroller.domain.service.TunnelCodeGenerator;
import org.junit.jupiter.api.Test;

class TunnelCodeGeneratorTest {
    @Test
    void convertsTunnelCodeToBase32TunnelId() {
        TunnelCodeGenerator generator = new TunnelCodeGenerator();

        assertThat(generator.toTunnelId(123456L)).isEqualTo("aaaadysa");
    }
}
