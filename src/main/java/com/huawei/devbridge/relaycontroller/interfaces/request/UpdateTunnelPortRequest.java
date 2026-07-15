package com.huawei.devbridge.relaycontroller.interfaces.request;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelProtocol;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTunnelPortRequest {
    private TunnelProtocol protocol;
    @NotNull
    private Boolean allowAnonymous;
}
