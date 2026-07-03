package com.huawei.devbridge.relaycontroller.interfaces.api;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.RegisterNodeRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.NodeInfoResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Gateway Node")
public interface NodeApi {

    @Operation(summary = "Register gateway node")
    @PostMapping("/{gridName}/node/register")
    Result<RegisterNodeResponse> register(
            @PathVariable String gridName,
            @Valid @RequestBody RegisterNodeRequest request);

    @Operation(summary = "Get gateway node IP")
    @GetMapping("/{gridName}/node")
    Result<NodeInfoResponse> getNode(
            @PathVariable String gridName,
            @RequestParam("node_id") String nodeId);
}
