package com.qq24650393.demo.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateListeningRequest(
        @NotNull Long relayDomainId,
        @Size(max = 64) String nodeCode,
        @Min(1) @Max(65535) int listenPort,
        @NotNull ListeningProtocol protocol,
        ListeningStatus status) {
}
