package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.BusinessException;
import com.qq24650393.demo.common.ErrorCode;
import java.time.Instant;
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
        Node node = repository.findByNodeCode(request.nodeCode()).orElseGet(Node::new);
        node.setNodeCode(request.nodeCode());
        node.setName(request.name());
        node.setAddress(request.address());
        node.setStatus(NodeStatus.ACTIVE);
        node.setLastHeartbeatAt(Instant.now());
        return NodeResponse.from(repository.save(node));
    }

    @Transactional
    public NodeResponse heartbeat(String nodeCode) {
        Node node = repository.findByNodeCode(nodeCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NODE_NOT_FOUND));
        node.setStatus(NodeStatus.ACTIVE);
        node.setLastHeartbeatAt(Instant.now());
        return NodeResponse.from(node);
    }

    @Transactional(readOnly = true)
    public List<NodeResponse> list() {
        return repository.findAll().stream()
                .map(NodeResponse::from)
                .toList();
    }

    Node findByCode(String nodeCode) {
        return repository.findByNodeCode(nodeCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NODE_NOT_FOUND));
    }
}
