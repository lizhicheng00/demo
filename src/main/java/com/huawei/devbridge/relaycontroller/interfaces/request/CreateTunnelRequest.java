package com.huawei.devbridge.relaycontroller.interfaces.request;

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
    private String gridname;
    private String cluster;
    private Integer expiration;
    private String type;
}
