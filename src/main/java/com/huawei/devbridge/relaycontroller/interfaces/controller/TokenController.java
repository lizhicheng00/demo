package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.TokenApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateRtTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateRtTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController implements TokenApi {
    private final TokenAppService tokenAppService;

    @Override
    public Result<CreateRtTokenResponse> createRtToken(String xUserId, CreateRtTokenRequest request) {
        return Result.success(tokenAppService.createRt(xUserId, request));
    }
}
