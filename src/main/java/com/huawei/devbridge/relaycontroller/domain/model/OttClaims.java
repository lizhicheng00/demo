package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OttClaims {
    private String jti;
    private String tunnelId;
    private Long tunnelCode;
    private String namespace;
    private String gridName;
    private String connId;
    private String callbackUrl;
    private Integer requestPort;
    private Long expiresAt;
}
