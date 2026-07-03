package com.huawei.devbridge.relaycontroller.interfaces.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOttTokenRequest {
    @NotBlank
    private String tunnelId;
    @JsonProperty("gridname")
    @JsonAlias("gridName")
    private String gridName;
    private String connId;
    private String callbackUrl;
    private Integer requestPort;
}
