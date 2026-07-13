package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metering {
    private Long id;
    private String clusterId;
    private Long tunnelCode;
    private String tunnelId;
    private Long usageBytes;
    private Long reportedAt;
    private Long createdAt;
}
