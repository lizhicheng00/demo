package com.huawei.devbridge.relaycontroller.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RelayStatusResponse {
    private String tunnelId;
    private String status;
    @JsonProperty("gridname")
    private String gridName;
    private String nodeId;
    private String gatewayIp;
    private Long lastHeartbeat;
}
