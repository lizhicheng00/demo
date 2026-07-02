package com.huawei.devbridge.relaycontroller.interfaces.response;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GridConfigResponse {
    private Map<String, String> jwtPublicKeys;
}
