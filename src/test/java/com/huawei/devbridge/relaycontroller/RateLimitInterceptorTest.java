package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.rate.RateLimitInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    @Test
    void shouldRejectRequestWhenRateLimitExceeded() throws Exception {
        RelayProperties properties = new RelayProperties();
        properties.getRateLimit().setRequestsPerMinute(2);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(properties, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Namespace", "ns-user-001");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, limitedResponse, new Object())).isFalse();
        assertThat(limitedResponse.getStatus()).isEqualTo(429);
        assertThat(limitedResponse.getContentAsString()).contains("\"error_code\":\"42900\"");
    }

    @Test
    void shouldAllowRequestWhenRateLimitDisabled() throws Exception {
        RelayProperties properties = new RelayProperties();
        properties.getRateLimit().setEnabled(false);
        RateLimitInterceptor interceptor = new RateLimitInterceptor(properties, new ObjectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
