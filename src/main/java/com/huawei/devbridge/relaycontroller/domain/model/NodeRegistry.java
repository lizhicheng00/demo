package com.huawei.devbridge.relaycontroller.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeRegistry {
    private Long id;
    private String gridName;
    private String ip;
    private Long registerTime;
    private Long createdAt;
    private Long updatedAt;
}
