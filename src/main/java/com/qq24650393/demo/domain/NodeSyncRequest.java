package com.qq24650393.demo.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NodeSyncRequest(
        @NotBlank @Size(max = 64) String nodeCode,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 255) String address) {
}
