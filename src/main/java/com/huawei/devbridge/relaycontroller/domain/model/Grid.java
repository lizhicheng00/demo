package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Grid {
    private Long id;
    private String grid;
    private String region;
    private Long createdAt;
    private Long updatedAt;
}
