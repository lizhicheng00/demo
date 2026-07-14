package com.huawei.devbridge.relaycontroller.interfaces.response;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelProtocol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelPortResponse {
    private String tunnelId;
    private Long tunnelCode;
    private Long port;
    private TunnelProtocol protocol;
    private Boolean allowAnonymous;
}
