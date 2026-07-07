package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.security.SccCrypto;
import org.junit.jupiter.api.Test;

class SccCryptoTest {

    @Test
    void shouldKeepPlainValueWhenKeyIsBlank() {
        SccCrypto crypto = new SccCrypto(new RelayProperties());

        assertThat(crypto.encrypt("token")).isEqualTo("token");
        assertThat(crypto.decrypt("token")).isEqualTo("token");
    }

    @Test
    void shouldEncryptAndDecryptValue() {
        RelayProperties properties = new RelayProperties();
        properties.getCrypto().setKey("test-key");
        SccCrypto crypto = new SccCrypto(properties);

        String encrypted = crypto.encrypt("token");

        assertThat(encrypted).startsWith("{scc}");
        assertThat(encrypted).isNotEqualTo("token");
        assertThat(crypto.decrypt(encrypted)).isEqualTo("token");
    }
}
