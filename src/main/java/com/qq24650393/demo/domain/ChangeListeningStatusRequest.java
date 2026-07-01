package com.qq24650393.demo.domain;

import jakarta.validation.constraints.NotNull;

public record ChangeListeningStatusRequest(@NotNull ListeningStatus status) {
}
