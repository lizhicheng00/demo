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
    private String gridname;
    private String ip;
    private Long registertime;
    private Long createdAt;
    private Long updatedAt;
}
