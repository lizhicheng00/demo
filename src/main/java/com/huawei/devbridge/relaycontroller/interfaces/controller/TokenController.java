package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.TokenApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateOttTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateRtTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateOttTokenResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateRtTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController implements TokenApi {
    private final TokenAppService tokenAppService;

    @Override
    public Result<CreateOttTokenResponse> createOttToken(CreateOttTokenRequest request) {
        return Result.success(tokenAppService.createOtt(request));
    }

    @Override
    public Result<CreateRtTokenResponse> createRtToken(String xRelayAuthorization, String xUserId, CreateRtTokenRequest request) {
        return Result.success(tokenAppService.createRt(xRelayAuthorization, xUserId, request));
    }
}
