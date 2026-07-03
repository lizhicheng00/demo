package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelayStatus {
    private String tunnelId;
    private String status;
    private String gridName;
    private String nodeId;
    private String gatewayIp;
    private Long lastHeartbeat;
}
