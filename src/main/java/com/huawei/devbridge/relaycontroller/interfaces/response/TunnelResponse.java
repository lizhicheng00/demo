package com.huawei.devbridge.relaycontroller.interfaces.response;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelResponse {
    private String name;
    private String tunnelId;
    private Long tunnelCode;
    private String clusterId;
    private String description;
    private Long bandwidthUsed;
    private Long expiration;
    private Long created;
    private String url;
    private TunnelType type;
}
