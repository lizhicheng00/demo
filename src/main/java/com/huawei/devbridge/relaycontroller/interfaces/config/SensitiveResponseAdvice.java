package com.huawei.devbridge.relaycontroller.interfaces.config;

import com.huawei.devbridge.relaycontroller.interfaces.controller.TunnelController;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelTokenResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(assignableTypes = TunnelController.class)
public class SensitiveResponseAdvice implements ResponseBodyAdvice<TunnelTokenResponse> {

    @Override
    public boolean supports(
            MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return TunnelTokenResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public TunnelTokenResponse beforeBodyWrite(
            TunnelTokenResponse body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        response.getHeaders().setCacheControl(CacheControl.noStore());
        response.getHeaders().setPragma("no-cache");
        return body;
    }
}
