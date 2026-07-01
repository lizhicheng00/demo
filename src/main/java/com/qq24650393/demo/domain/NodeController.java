package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

    private final NodeService service;

    public NodeController(NodeService service) {
        this.service = service;
    }

    @PostMapping("/sync")
    public ApiResponse<NodeResponse> sync(@Valid @RequestBody NodeSyncRequest request) {
        return ApiResponse.ok(service.sync(request));
    }

    @PostMapping("/{nodeCode}/heartbeat")
    public ApiResponse<NodeResponse> heartbeat(
            @PathVariable String nodeCode,
            @Valid @RequestBody(required = false) NodeHeartbeatRequest request) {
        return ApiResponse.ok(service.heartbeat(nodeCode));
    }

    @GetMapping
    public ApiResponse<List<NodeResponse>> list() {
        return ApiResponse.ok(service.list());
    }
}
