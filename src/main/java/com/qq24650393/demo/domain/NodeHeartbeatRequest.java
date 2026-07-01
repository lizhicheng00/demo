package com.qq24650393.demo.domain;

import jakarta.validation.constraints.Min;

public record NodeHeartbeatRequest(@Min(0) long listeningVersion) {
}
