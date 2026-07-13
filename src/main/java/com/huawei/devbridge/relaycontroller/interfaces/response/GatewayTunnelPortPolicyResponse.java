package com.huawei.devbridge.relaycontroller.interfaces.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatewayTunnelPortPolicyResponse {
    private String tunnelId;
    private Long tunnelCode;
    private String clusterId;
    private Long port;
    private Boolean allowAnonymous;
}
