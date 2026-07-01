package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.BusinessException;
import com.qq24650393.demo.common.ErrorCode;
import com.qq24650393.demo.web.model.NodeResponse;
import com.qq24650393.demo.web.model.NodeSyncRequest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NodeService {

    private final NodeRepository repository;

    public NodeService(NodeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NodeResponse sync(NodeSyncRequest request) {
        Node node = repository.findByNodeCode(request.getNodeCode()).orElseGet(Node::new);
        node.setNodeCode(request.getNodeCode());
        node.setName(request.getName());
        node.setAddress(request.getAddress());
        node.setStatus(NodeStatus.ACTIVE);
        node.setLastHeartbeatAt(Instant.now());
        if (node.getId() == null) {
            repository.insert(node);
        } else {
            repository.update(node);
        }
        return toResponse(repository.findByNodeCode(node.getNodeCode()).orElseThrow());
    }

    @Transactional
    public NodeResponse heartbeat(String nodeCode) {
        Node node = repository.findByNodeCode(nodeCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NODE_NOT_FOUND));
        node.setStatus(NodeStatus.ACTIVE);
        node.setLastHeartbeatAt(Instant.now());
        repository.update(node);
        return toResponse(repository.findByNodeCode(nodeCode).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<NodeResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    Node findByCode(String nodeCode) {
        return repository.findByNodeCode(nodeCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NODE_NOT_FOUND));
    }

    private NodeResponse toResponse(Node node) {
        return new NodeResponse()
                .id(node.getId())
                .nodeCode(node.getNodeCode())
                .name(node.getName())
                .address(node.getAddress())
                .status(com.qq24650393.demo.web.model.NodeStatus.fromValue(node.getStatus().name()))
                .lastHeartbeatAt(node.getLastHeartbeatAt() == null ? null : node.getLastHeartbeatAt().atOffset(ZoneOffset.UTC))
                .createdAt(node.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(node.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }
}
