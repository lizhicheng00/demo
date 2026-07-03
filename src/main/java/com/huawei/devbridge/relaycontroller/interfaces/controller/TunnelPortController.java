package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TunnelPortAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.GatewayTunnelPortApi;
import com.huawei.devbridge.relaycontroller.generated.api.TunnelPortApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelPortRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.GatewayTunnelPortPolicyResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelPortResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TunnelPortController implements TunnelPortApi, GatewayTunnelPortApi {
    private final TunnelPortAppService tunnelPortAppService;

    @Override
    public Result<TunnelPortResponse> createTunnelPort(String xUserId, String tunnelId, CreateTunnelPortRequest request) {
        return Result.success(tunnelPortAppService.create(xUserId, tunnelId, request));
    }

    @Override
    public Result<Boolean> deleteTunnelPort(String xUserId, String tunnelId, Long port) {
        return Result.success(tunnelPortAppService.delete(xUserId, tunnelId, port));
    }

    @Override
    public Result<TunnelPortResponse> getTunnelPort(String xUserId, String tunnelId, Long port) {
        return Result.success(tunnelPortAppService.detail(xUserId, tunnelId, port));
    }

    @Override
    public Result<GatewayTunnelPortPolicyResponse> getGatewayTunnelPortPolicy(String gridName, String tunnelId, Long port) {
        return Result.success(tunnelPortAppService.getGatewayPortPolicy(gridName, tunnelId, port));
    }

    @Override
    public Result<List<TunnelPortResponse>> listTunnelPorts(String xUserId, String tunnelId) {
        return Result.success(tunnelPortAppService.list(xUserId, tunnelId));
    }

    @Override
    public Result<TunnelPortResponse> updateTunnelPort(String xUserId, String tunnelId, Long port, UpdateTunnelPortRequest request) {
        return Result.success(tunnelPortAppService.update(xUserId, tunnelId, port, request));
    }
}
