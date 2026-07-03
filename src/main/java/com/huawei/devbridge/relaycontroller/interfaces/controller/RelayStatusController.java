package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.RelayStatusAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.response.RelayStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Relay Status")
@RestController
@RequestMapping("/v1/tunnel")
@RequiredArgsConstructor
public class RelayStatusController {
    private static final String USER_HEADER = "X-User-Id";
    private final RelayStatusAppService relayStatusAppService;

    @Operation(summary = "Get tunnel runtime relay status")
    @GetMapping("/status")
    public Result<RelayStatusResponse> status(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId) {
        return Result.success(relayStatusAppService.getStatus(userId, tunnelId));
    }
}
