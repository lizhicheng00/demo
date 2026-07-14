package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.infrastructure.security.JwtKeyProvider;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtKeyProviderTest {

    @Test
    void rejectsMissingKeyByDefault() {
        JwtKeyProvider provider = new JwtKeyProvider(new RelayProperties());

        assertThatThrownBy(provider::init)
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.JWT_KEY_INVALID);
    }

    @Test
    void allowsEphemeralKeyWhenExplicitlyEnabled() {
        RelayProperties properties = new RelayProperties();
        properties.getJwt().setAllowEphemeralKey(true);
        JwtKeyProvider provider = new JwtKeyProvider(properties);

        provider.init();

        assertThat(provider.getPrivateKey()).isNotNull();
    }

    @Test
    void parsesConfiguredPkcs8Key() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        RelayProperties properties = new RelayProperties();
        properties.getJwt().setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        JwtKeyProvider provider = new JwtKeyProvider(properties);

        provider.init();

        assertThat(provider.getPrivateKey().getEncoded()).isEqualTo(keyPair.getPrivate().getEncoded());
    }
}
