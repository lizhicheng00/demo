package com.huawei.devbridge.relaycontroller.interfaces.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTunnelPortRequest {
    @NotNull
    @Min(1)
    @Max(65535)
    private Long port;
    @NotNull
    private Boolean allowAnonymous;
}
