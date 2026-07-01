package com.qq24650393.demo.domain;

import java.time.Instant;

public record NodeResponse(
        Long id,
        String nodeCode,
        String name,
        String address,
        NodeStatus status,
        Instant lastHeartbeatAt,
        Instant createdAt,
        Instant updatedAt) {

    public static NodeResponse from(Node node) {
        return new NodeResponse(
                node.getId(),
                node.getNodeCode(),
                node.getName(),
                node.getAddress(),
                node.getStatus(),
                node.getLastHeartbeatAt(),
                node.getCreatedAt(),
                node.getUpdatedAt());
    }
}
