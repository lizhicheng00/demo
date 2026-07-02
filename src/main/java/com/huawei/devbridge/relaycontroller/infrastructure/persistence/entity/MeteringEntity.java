package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("metering")
public class MeteringEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    @TableField("gridname")
    private String gridname;
    @TableField("tunnelcode")
    private Long tunnelcode;
    @TableField("tunnelid")
    private String tunnelid;
    @TableField("usage_bytes")
    private Long usageBytes;
    @TableField("reported_at")
    private Long reportedAt;
    @TableField("created_at")
    private Long createdAt;
}
