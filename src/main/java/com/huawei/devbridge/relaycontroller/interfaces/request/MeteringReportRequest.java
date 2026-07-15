package com.huawei.devbridge.relaycontroller.interfaces.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MeteringReportRequest {
    @NotNull
    @Min(1)
    @Max(1_099_511_627_775L)
    private Long tunnelCode;
    @NotBlank
    @Pattern(regexp = "^[a-z2-7]{8}$")
    private String tunnelId;
    @NotNull
    @Min(0)
    private Long usage;
}
