package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import org.junit.jupiter.api.Test;

class SccCryptoTest {

    @Test
    void shouldKeepPlainValueWhenEncrypting() {
        SccCrypto crypto = new SccCrypto();

        assertThat(crypto.encrypt("token")).isEqualTo("token");
    }

    @Test
    void shouldStripSccPrefixWhenDecrypting() {
        SccCrypto crypto = new SccCrypto();

        assertThat(crypto.decrypt("{scc}token")).isEqualTo("token");
        assertThat(crypto.decrypt("token")).isEqualTo("token");
    }
}
