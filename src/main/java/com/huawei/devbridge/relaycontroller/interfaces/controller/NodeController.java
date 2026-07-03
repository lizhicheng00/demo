package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.NodeAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.api.NodeApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.RegisterNodeRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.NodeInfoResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NodeController implements NodeApi {
    private final NodeAppService nodeAppService;

    @Override
    public Result<RegisterNodeResponse> register(String gridName, RegisterNodeRequest request) {
        return Result.success(nodeAppService.registerNode(gridName, request));
    }

    @Override
    public Result<NodeInfoResponse> getNode(String gridName, String nodeId) {
        return Result.success(nodeAppService.getNode(gridName, nodeId));
    }
}
