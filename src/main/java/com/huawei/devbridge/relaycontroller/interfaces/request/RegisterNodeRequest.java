package com.huawei.devbridge.relaycontroller.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterNodeRequest {
    @NotBlank
    private String ip;
    private String nodeId;
}
