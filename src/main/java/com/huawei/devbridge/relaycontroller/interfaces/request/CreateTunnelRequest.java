package com.huawei.devbridge.relaycontroller.interfaces.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTunnelRequest {
    @NotBlank
    @Size(max = 128)
    private String name;
    @Size(max = 512)
    private String description;
    @NotBlank
    @JsonProperty("gridname")
    @JsonAlias("gridName")
    private String gridName;
    private String cluster;
    private Integer expiration;
    private TunnelType type;
}
