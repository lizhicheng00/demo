package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.TunnelAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.api.TunnelApi;
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
    public Result<List<TunnelListItemResponse>> list(String userId, String gridName) {
        return Result.success(tunnelAppService.listTunnels(userId, gridName));
    }

    @Override
    public Result<TunnelDetailResponse> detail(String userId, String tunnelId) {
        return Result.success(tunnelAppService.getTunnelDetail(userId, tunnelId));
    }

    @Override
    public Result<CreateTunnelResponse> create(String userId, CreateTunnelRequest request) {
        return Result.success(tunnelAppService.createTunnel(userId, request));
    }

    @Override
    public Result<Boolean> update(String userId, UpdateTunnelRequest request) {
        return Result.success(tunnelAppService.updateTunnel(userId, request));
    }

    @Override
    public Result<Boolean> delete(String userId, String tunnelId) {
        return Result.success(tunnelAppService.deleteTunnel(userId, tunnelId));
    }
}
