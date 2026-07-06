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
    public Result<TunnelPortResponse> createTunnelPort(String xNamespace, String tunnelId, CreateTunnelPortRequest request) {
        return Result.success(tunnelPortAppService.create(xNamespace, tunnelId, request));
    }

    @Override
    public Result<Boolean> deleteTunnelPort(String xNamespace, String tunnelId, Long port) {
        return Result.success(tunnelPortAppService.delete(xNamespace, tunnelId, port));
    }

    @Override
    public Result<Boolean> deleteTunnelPorts(String xNamespace, String tunnelId) {
        return Result.success(tunnelPortAppService.deleteAll(xNamespace, tunnelId));
    }

    @Override
    public Result<TunnelPortResponse> getTunnelPort(String xNamespace, String tunnelId, Long port) {
        return Result.success(tunnelPortAppService.detail(xNamespace, tunnelId, port));
    }

    @Override
    public Result<GatewayTunnelPortPolicyResponse> getGatewayTunnelPortPolicy(String gridName, String tunnelId, Long port) {
        return Result.success(tunnelPortAppService.getGatewayPortPolicy(gridName, tunnelId, port));
    }

    @Override
    public Result<List<TunnelPortResponse>> listTunnelPorts(String xNamespace, String tunnelId) {
        return Result.success(tunnelPortAppService.list(xNamespace, tunnelId));
    }

    @Override
    public Result<TunnelPortResponse> updateTunnelPort(String xNamespace, String tunnelId, Long port, UpdateTunnelPortRequest request) {
        return Result.success(tunnelPortAppService.update(xNamespace, tunnelId, port, request));
    }
}
