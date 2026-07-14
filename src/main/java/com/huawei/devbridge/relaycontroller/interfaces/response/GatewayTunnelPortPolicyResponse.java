package com.huawei.devbridge.relaycontroller.interfaces.response;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelProtocol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatewayTunnelPortPolicyResponse {
    private String tunnelId;
    private Long tunnelCode;
    private String clusterId;
    private Long port;
    private TunnelProtocol protocol;
    private Boolean allowAnonymous;
}
