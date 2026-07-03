package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.NodeAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.GatewayNodeApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.RegisterNodeRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.NodeInfoResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NodeController implements GatewayNodeApi {
    private final NodeAppService nodeAppService;

    @Override
    public Result<NodeInfoResponse> getNode(String gridName, String nodeId) {
        return Result.success(nodeAppService.getNode(gridName, nodeId));
    }

    @Override
    public Result<RegisterNodeResponse> registerNode(String gridName, RegisterNodeRequest request) {
        return Result.success(nodeAppService.registerNode(gridName, request));
    }
}
