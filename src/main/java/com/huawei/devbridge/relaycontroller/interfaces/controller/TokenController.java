package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TokenAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateOttTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateRtTokenRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateOttTokenResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateRtTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token")
@Validated
@RestController
@RequestMapping("/open-api-inner/v1/relay-controller/tokens")
@RequiredArgsConstructor
public class TokenController {
    private static final String RELAY_AUTHORIZATION_HEADER = "X-Relay-Authorization";
    private static final String USER_HEADER = "X-User-Id";
    private final TokenAppService tokenAppService;

    @Operation(summary = "Create one-time token")
    @PostMapping("/ott")
    public Result<CreateOttTokenResponse> createOtt(@Valid @RequestBody CreateOttTokenRequest request) {
        return Result.success(tokenAppService.createOtt(request));
    }

    @Operation(summary = "Create reusable token")
    @PostMapping("/rt")
    public Result<CreateRtTokenResponse> createRt(
            @RequestHeader(value = RELAY_AUTHORIZATION_HEADER, required = false) String relayAuthorization,
            @RequestHeader(value = USER_HEADER, required = false) String userId,
            @RequestBody(required = false) CreateRtTokenRequest request) {
        return Result.success(tokenAppService.createRt(relayAuthorization, userId, request));
    }
}
