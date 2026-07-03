package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("node_registry")
public class NodeRegistryEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    private String gridName;
    private String ip;
    private Long registerTime;
    private Long createdAt;
    private Long updatedAt;
}
