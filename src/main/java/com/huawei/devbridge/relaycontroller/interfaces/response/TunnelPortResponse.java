package com.huawei.devbridge.relaycontroller.interfaces.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelPortResponse {
    private Long id;
    private String tunnelId;
    private Long tunnelCode;
    private Long port;
    private Boolean allowAnonymous;
}
