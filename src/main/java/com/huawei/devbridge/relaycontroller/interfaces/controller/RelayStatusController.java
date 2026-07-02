package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.RelayStatusAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.response.RelayStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RelayStatusController {
    private static final String USER_HEADER = "X-User-Id";
    private final RelayStatusAppService relayStatusAppService;

    @GetMapping("/tunnel/status")
    public Result<RelayStatusResponse> status(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId) {
        return Result.success(relayStatusAppService.getStatus(userId, tunnelId));
    }
}
