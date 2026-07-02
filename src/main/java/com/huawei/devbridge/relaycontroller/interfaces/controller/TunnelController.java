package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class TunnelController {
    private static final String USER_HEADER = "X-User-Id";
    private final TunnelAppService tunnelAppService;

    @GetMapping("/tunnels")
    public Result<List<TunnelListItemResponse>> list(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam(required = false) String gridname) {
        return Result.success(tunnelAppService.listTunnels(userId, gridname));
    }

    @GetMapping("/tunnel")
    public Result<TunnelDetailResponse> detail(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId) {
        return Result.success(tunnelAppService.getTunnelDetail(userId, tunnelId));
    }

    @PostMapping("/tunnel")
    public Result<CreateTunnelResponse> create(
            @RequestHeader(USER_HEADER) String userId,
            @Valid @RequestBody CreateTunnelRequest request) {
        return Result.success(tunnelAppService.createTunnel(userId, request));
    }

    @PutMapping("/tunnel")
    public Result<Boolean> update(
            @RequestHeader(USER_HEADER) String userId,
            @Valid @RequestBody UpdateTunnelRequest request) {
        return Result.success(tunnelAppService.updateTunnel(userId, request));
    }

    @DeleteMapping("/tunnel")
    public Result<Boolean> delete(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId) {
        return Result.success(tunnelAppService.deleteTunnel(userId, tunnelId));
    }
}
