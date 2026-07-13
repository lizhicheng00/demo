package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("metering")
public class MeteringEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    private String clusterId;
    private Long tunnelCode;
    private String tunnelId;
    private Long usageBytes;
    private Long reportedAt;
    private Long createdAt;
}
