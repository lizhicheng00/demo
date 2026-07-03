package com.huawei.devbridge.relaycontroller.interfaces.api;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.response.RelayStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Relay Status")
public interface RelayStatusApi {
    String USER_HEADER = "X-User-Id";

    @Operation(summary = "Get tunnel runtime relay status")
    @GetMapping("/tunnel/status")
    Result<RelayStatusResponse> status(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId);
}
