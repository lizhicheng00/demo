package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tunnel")
public class TunnelEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    private String name;
    @TableField("tunnelid")
    private String tunnelid;
    @TableField("tunnelcode")
    private Long tunnelcode;
    @TableField("gridname")
    private String gridname;
    private Integer expiration;
    private String namespace;
    private String description;
    private String cluster;
    @TableField("bandwidthused")
    private Long bandwidthused;
    private String url;
    private String type;
    private Integer deleted;
    @TableField("created_at")
    private Long createdAt;
    @TableField("updated_at")
    private Long updatedAt;
}
