package com.qq24650393.demo.domain;

import java.time.Instant;

public record RelayDomainResponse(
        Long id,
        String domain,
        String targetUrl,
        RelayDomainStatus status,
        String remark,
        Instant createdAt,
        Instant updatedAt) {

    public static RelayDomainResponse from(RelayDomain relayDomain) {
        return new RelayDomainResponse(
                relayDomain.getId(),
                relayDomain.getDomain(),
                relayDomain.getTargetUrl(),
                relayDomain.getStatus(),
                relayDomain.getRemark(),
                relayDomain.getCreatedAt(),
                relayDomain.getUpdatedAt());
    }
}
