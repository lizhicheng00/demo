package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.huawei.clouds.wushan.scc.crypto.SccCrypto;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.security.JwtKeyProvider;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtKeyProviderTest {

    @Test
    void shouldRequireConfiguredKeyByDefault() {
        JwtKeyProvider provider = new JwtKeyProvider(new RelayProperties(), mock(SccCrypto.class));

        assertThatThrownBy(provider::init)
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.JWT_KEY_INVALID);
    }

    @Test
    void shouldLoadConfigured2048BitKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String encodedKey = Base64.getEncoder().encodeToString(generator.generateKeyPair().getPrivate().getEncoded());

        RelayProperties properties = new RelayProperties();
        properties.getJwt().setPrivateKey("encrypted-key");
        SccCrypto sccCrypto = mock(SccCrypto.class);
        when(sccCrypto.decrypt("encrypted-key")).thenReturn(encodedKey);
        JwtKeyProvider provider = new JwtKeyProvider(properties, sccCrypto);

        provider.init();

        assertThat(provider.getPrivateKey()).isInstanceOf(RSAPrivateKey.class);
        RSAPrivateKey privateKey = (RSAPrivateKey) provider.getPrivateKey();
        assertThat(privateKey.getModulus().bitLength()).isGreaterThanOrEqualTo(2048);
    }

    @Test
    void shouldRejectRsaKeySmallerThan2048Bits() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        String encodedKey = Base64.getEncoder().encodeToString(generator.generateKeyPair().getPrivate().getEncoded());

        RelayProperties properties = new RelayProperties();
        properties.getJwt().setPrivateKey("encrypted-key");
        SccCrypto sccCrypto = mock(SccCrypto.class);
        when(sccCrypto.decrypt("encrypted-key")).thenReturn(encodedKey);
        JwtKeyProvider provider = new JwtKeyProvider(properties, sccCrypto);

        assertThatThrownBy(provider::init)
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.JWT_KEY_INVALID);
    }
}
