package com.huawei.devbridge.relaycontroller.interfaces.request;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelProtocol;
import lombok.Data;

@Data
public class UpdateTunnelPortRequest {
    private TunnelProtocol protocol;
    private Boolean allowAnonymous;
}
