package com.huawei.devbridge.relaycontroller.interfaces.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTunnelPortRequest {
    @NotNull
    private Long port;
    @NotNull
    private Boolean allowAnonymous;
}
