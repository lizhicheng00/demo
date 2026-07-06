package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.TokenApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController implements TokenApi {
    private final TokenAppService tokenAppService;

    @Override
    public Result<CreateTokenResponse> createToken(String xUserId, CreateTokenRequest request) {
        return Result.success(tokenAppService.createToken(xUserId, request));
    }
}
