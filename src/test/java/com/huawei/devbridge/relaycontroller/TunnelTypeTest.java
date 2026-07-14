package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import org.junit.jupiter.api.Test;

class TunnelTypeTest {
    @Test
    void parsesTypeIgnoringCase() {
        assertThat(TunnelType.fromValue("BrIdGe")).isEqualTo(TunnelType.BRIDGE);
    }

    @Test
    void rejectsBlankType() {
        assertThatThrownBy(() -> TunnelType.fromValue("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
