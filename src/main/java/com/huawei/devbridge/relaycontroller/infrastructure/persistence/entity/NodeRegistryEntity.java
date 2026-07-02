package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("node_registry")
public class NodeRegistryEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    @TableField("gridname")
    private String gridname;
    private String ip;
    @TableField("registertime")
    private Long registertime;
    @TableField("created_at")
    private Long createdAt;
    @TableField("updated_at")
    private Long updatedAt;
}
