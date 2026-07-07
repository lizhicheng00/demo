package com.huawei.devbridge.relaycontroller.interfaces.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelDetailResponse {
    private String name;
    private String tunnelId;
    private Long tunnelCode;
    private String gridName;
    private String cluster;
    private String description;
    private Long bandwidthUsed;
    private Integer expiration;
    private Long created;
    private String url;
    private String type;
    private JwtResponse jwt;
}
