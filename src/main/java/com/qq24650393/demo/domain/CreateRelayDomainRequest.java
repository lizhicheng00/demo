package com.qq24650393.demo.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRelayDomainRequest(
        @NotBlank @Size(max = 255) String domain,
        @NotBlank @Size(max = 512) String targetUrl,
        @Size(max = 512) String remark) {
}
