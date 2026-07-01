package com.qq24650393.demo.ops;

public record OpsOverviewResponse(
        long totalRelayDomains,
        long enabledRelayDomains,
        long totalNodes,
        long activeNodes,
        long enabledListenings,
        long inboundBytes24h,
        long outboundBytes24h,
        long activeConnections24h) {
}
