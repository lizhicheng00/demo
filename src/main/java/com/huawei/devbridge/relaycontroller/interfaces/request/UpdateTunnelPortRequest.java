package com.huawei.devbridge.relaycontroller.interfaces.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTunnelPortRequest {
    @NotNull
    private Boolean allowAnonymous;
}
