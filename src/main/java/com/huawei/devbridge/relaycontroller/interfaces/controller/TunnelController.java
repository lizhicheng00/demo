package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.TunnelApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TunnelController implements TunnelApi {
    private final TunnelAppService tunnelAppService;

    @Override
    public Result<CreateTunnelResponse> createTunnel(String xNamespace, CreateTunnelRequest request) {
        return Result.success(tunnelAppService.createTunnel(xNamespace, request));
    }

    @Override
    public Result<Boolean> deleteTunnel(String xNamespace, String tunnelId) {
        return Result.success(tunnelAppService.deleteTunnel(xNamespace, tunnelId));
    }

    @Override
    public Result<Boolean> deleteTunnels(String xNamespace) {
        return Result.success(tunnelAppService.deleteTunnels(xNamespace));
    }

    @Override
    public Result<TunnelDetailResponse> getTunnelDetail(String xNamespace, String tunnelId) {
        return Result.success(tunnelAppService.getTunnelDetail(xNamespace, tunnelId));
    }

    @Override
    public Result<List<TunnelListItemResponse>> listTunnels(String xNamespace, String gridName) {
        return Result.success(tunnelAppService.listTunnels(xNamespace, gridName));
    }

    @Override
    public Result<Boolean> updateTunnel(String xNamespace, String tunnelId, UpdateTunnelRequest request) {
        request.setTunnelId(tunnelId);
        return Result.success(tunnelAppService.updateTunnel(xNamespace, request));
    }
}
