package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.NodeAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.RegisterNodeRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.NodeInfoResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gateway Node")
@RestController
@RequestMapping("/v1/tunnel")
@RequiredArgsConstructor
public class NodeController {
    private final NodeAppService nodeAppService;

    @Operation(summary = "Register gateway node")
    @PostMapping("/{gridName}/node/register")
    public Result<RegisterNodeResponse> register(
            @PathVariable String gridName,
            @Valid @RequestBody RegisterNodeRequest request) {
        return Result.success(nodeAppService.registerNode(gridName, request));
    }

    @Operation(summary = "Get gateway node IP")
    @GetMapping("/{gridName}/node")
    public Result<NodeInfoResponse> getNode(
            @PathVariable String gridName,
            @RequestParam("node_id") String nodeId) {
        return Result.success(nodeAppService.getNode(gridName, nodeId));
    }
}
