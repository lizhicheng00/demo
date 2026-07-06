package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tunnel {
    private Long id;
    private String name;
    private String tunnelId;
    private Long tunnelCode;
    private String gridName;
    private Integer expiration;
    private String namespace;
    private String description;
    private String cluster;
    private Long bandwidthUsed;
    private String url;
    private TunnelType type;
    private Integer deleted;
    private Long createdAt;
    private Long updatedAt;
}
