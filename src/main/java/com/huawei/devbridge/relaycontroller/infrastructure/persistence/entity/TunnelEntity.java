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
    @TableField("name")
    private String name;
    @TableField("tunnelid")
    private String tunnelid;
    @TableField("tunnelcode")
    private Long tunnelcode;
    @TableField("gridname")
    private String gridname;
    @TableField("expiration")
    private Integer expiration;
    @TableField("namespace")
    private String namespace;
    @TableField("description")
    private String description;
    @TableField("cluster")
    private String cluster;
    @TableField("bandwidthused")
    private Long bandwidthused;
    @TableField("url")
    private String url;
    @TableField("type")
    private String type;
    @TableField("deleted")
    private Integer deleted;
    @TableField("created_at")
    private Long createdAt;
    @TableField("updated_at")
    private Long updatedAt;
}
