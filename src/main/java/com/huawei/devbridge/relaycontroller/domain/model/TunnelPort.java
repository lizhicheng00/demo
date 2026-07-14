package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TunnelPort {
    private Long id;
    private Long tunnelCode;
    private Long port;
    private TunnelProtocol protocol;
    private Boolean allowAnonymous;
}
