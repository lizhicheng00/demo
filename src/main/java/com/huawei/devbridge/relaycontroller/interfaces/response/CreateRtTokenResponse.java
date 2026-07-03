package com.huawei.devbridge.relaycontroller.interfaces.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRtTokenResponse {
    private String tokenType;
    private String token;
    private Long expiresIn;
}
