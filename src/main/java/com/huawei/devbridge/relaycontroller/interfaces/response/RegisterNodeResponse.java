package com.huawei.devbridge.relaycontroller.interfaces.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterNodeResponse {
    private String nodeId;
    private List<String> nodeList;
}
