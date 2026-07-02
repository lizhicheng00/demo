package com.huawei.devbridge.relaycontroller.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTunnelRequest {
    @NotBlank
    private String tunnelId;
    @Size(max = 128)
    private String name;
    @Size(max = 512)
    private String description;
    private String cluster;
    private Integer expiration;
    private String type;
}
