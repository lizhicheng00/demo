package com.huawei.devbridge.relaycontroller.interfaces.request;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTunnelRequest {
    @Size(max = 128)
    @Pattern(regexp = ".*\\S.*", message = "must not be blank")
    private String name;
    @Size(max = 512)
    private String description;
    @Min(1)
    @Max(720)
    private Integer expiration;
    private TunnelType type;
}
