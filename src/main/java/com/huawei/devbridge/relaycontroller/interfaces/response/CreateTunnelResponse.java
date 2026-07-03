package com.huawei.devbridge.relaycontroller.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTunnelResponse {
    private String name;
    private String id;
    private String tunnelId;
    private Long tunnelCode;
    @JsonProperty("gridname")
    private String gridName;
    private String cluster;
    private String description;
    private Long bandwidthUsed;
    private Integer expiration;
    private Long created;
    private String url;
    private String type;
}
