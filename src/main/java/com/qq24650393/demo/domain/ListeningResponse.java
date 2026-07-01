package com.qq24650393.demo.domain;

import java.time.Instant;

public record ListeningResponse(
        Long id,
        Long relayDomainId,
        String domain,
        String targetUrl,
        String nodeCode,
        int listenPort,
        ListeningProtocol protocol,
        ListeningStatus status,
        long version,
        Instant updatedAt) {

    public static ListeningResponse from(ListeningConfig config) {
        Node node = config.getNode();
        RelayDomain relayDomain = config.getRelayDomain();
        return new ListeningResponse(
                config.getId(),
                relayDomain.getId(),
                relayDomain.getDomain(),
                relayDomain.getTargetUrl(),
                node == null ? null : node.getNodeCode(),
                config.getListenPort(),
                config.getProtocol(),
                config.getStatus(),
                config.getVersion(),
                config.getUpdatedAt());
    }
}
