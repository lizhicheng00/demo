package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cluster")
public class ClusterEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    private String clusterId;
    private String region;
    private Long createdAt;
    private Long updatedAt;
}
