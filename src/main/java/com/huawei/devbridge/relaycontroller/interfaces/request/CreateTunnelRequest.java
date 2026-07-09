package com.huawei.devbridge.relaycontroller.interfaces.request;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    private String gridName;
    private String cluster;
    @Min(1)
    @Max(720)
    private Integer expiration;
    private TunnelType type;
}
