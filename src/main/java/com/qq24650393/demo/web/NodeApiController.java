package com.qq24650393.demo.web;

import com.qq24650393.demo.domain.NodeService;
import com.qq24650393.demo.web.api.NodeApi;
import com.qq24650393.demo.web.model.ApiResponseNodeList;
import com.qq24650393.demo.web.model.ApiResponseNodeResponse;
import com.qq24650393.demo.web.model.NodeHeartbeatRequest;
import com.qq24650393.demo.web.model.NodeSyncRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NodeApiController implements NodeApi {

    private final NodeService service;

    public NodeApiController(NodeService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<ApiResponseNodeResponse> syncNode(NodeSyncRequest request) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseNodeResponse().data(service.sync(request))));
    }

    @Override
    public ResponseEntity<ApiResponseNodeResponse> heartbeatNode(String nodeCode, NodeHeartbeatRequest request) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseNodeResponse().data(service.heartbeat(nodeCode))));
    }

    @Override
    public ResponseEntity<ApiResponseNodeList> listNodes() {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseNodeList().data(service.list())));
    }
}
