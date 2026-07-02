package com.huawei.devbridge.relaycontroller.interfaces.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NodeInfoResponse {
    private String ip;
}
