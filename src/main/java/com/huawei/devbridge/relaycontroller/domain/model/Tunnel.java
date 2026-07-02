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
    private String tunnelid;
    private Long tunnelcode;
    private String gridname;
    private Integer expiration;
    private String namespace;
    private String description;
    private String cluster;
    private Long bandwidthused;
    private String url;
    private String type;
    private Integer deleted;
    private Long createdAt;
    private Long updatedAt;
}
