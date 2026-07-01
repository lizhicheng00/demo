package com.qq24650393.demo.ops;

import java.time.Instant;

public record TrafficPointResponse(
        Instant capturedAt,
        String nodeCode,
        long inboundBytes,
        long outboundBytes,
        int activeConnections) {

    static TrafficPointResponse from(TrafficSnapshot snapshot) {
        return new TrafficPointResponse(
                snapshot.getCapturedAt(),
                snapshot.getNode() == null ? null : snapshot.getNode().getNodeCode(),
                snapshot.getInboundBytes(),
                snapshot.getOutboundBytes(),
                snapshot.getActiveConnections());
    }
}
