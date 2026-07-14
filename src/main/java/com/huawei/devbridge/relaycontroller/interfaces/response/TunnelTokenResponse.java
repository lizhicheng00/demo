package com.huawei.devbridge.relaycontroller.interfaces.response;

import com.huawei.devbridge.relaycontroller.domain.model.JwtScope;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TunnelTokenResponse {
    private String tunnelId;
    private JwtScope scope;
    private Long lifetime;
    private Long expiration;
    private String token;
}
