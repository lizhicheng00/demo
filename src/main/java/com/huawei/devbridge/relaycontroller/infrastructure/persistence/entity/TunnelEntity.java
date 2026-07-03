package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tunnel")
public class TunnelEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String tunnelId;
    private Long tunnelCode;
    private String gridName;
    private Integer expiration;
    private String namespace;
    private String description;
    private String cluster;
    private Long bandwidthUsed;
    private String url;
    private String type;
    private Integer deleted;
    private Long createdAt;
    private Long updatedAt;
}
