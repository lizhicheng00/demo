package com.huawei.devbridge.relaycontroller.interfaces.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelListItemResponse {
    private String tunnelId;
    private Long tunnelCode;
    private String clusterId;
    private String name;
    private String description;
    private Integer expiration;
    private Long created;
    private String url;
}
