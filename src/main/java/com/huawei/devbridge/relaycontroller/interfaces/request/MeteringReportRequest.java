package com.huawei.devbridge.relaycontroller.interfaces.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MeteringReportRequest {
    @NotNull
    private Long tunnelCode;
    @NotBlank
    private String tunnelId;
    @NotNull
    @Min(0)
    private Long usage;
}
