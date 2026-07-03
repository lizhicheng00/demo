package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tunnel")
@Validated
@RestController
@RequestMapping("/v1/tunnel")
@RequiredArgsConstructor
public class TunnelController {
    private static final String USER_HEADER = "X-User-Id";
    private final TunnelAppService tunnelAppService;

    @Operation(summary = "List user tunnels")
    @GetMapping("/list")
    public Result<List<TunnelListItemResponse>> list(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam(required = false) String gridName) {
        return Result.success(tunnelAppService.listTunnels(userId, gridName));
    }

    @Operation(summary = "Get tunnel detail with access token")
    @GetMapping
    public Result<TunnelDetailResponse> detail(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId) {
        return Result.success(tunnelAppService.getTunnelDetail(userId, tunnelId));
    }

    @Operation(summary = "Create tunnel")
    @PostMapping
    public Result<CreateTunnelResponse> create(
            @RequestHeader(USER_HEADER) String userId,
            @Valid @RequestBody CreateTunnelRequest request) {
        return Result.success(tunnelAppService.createTunnel(userId, request));
    }

    @Operation(summary = "Update tunnel")
    @PutMapping
    public Result<Boolean> update(
            @RequestHeader(USER_HEADER) String userId,
            @Valid @RequestBody UpdateTunnelRequest request) {
        return Result.success(tunnelAppService.updateTunnel(userId, request));
    }

    @Operation(summary = "Soft delete tunnel")
    @DeleteMapping
    public Result<Boolean> delete(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId) {
        return Result.success(tunnelAppService.deleteTunnel(userId, tunnelId));
    }
}
