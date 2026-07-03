package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOttTokenCommand {
    private String jti;
    private Tunnel tunnel;
    private String connId;
    private String callbackUrl;
    private Integer requestPort;
}
