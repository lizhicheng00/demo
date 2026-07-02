package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("grid")
public class GridEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    private String grid;
    private String region;
    @TableField("created_at")
    private Long createdAt;
    @TableField("updated_at")
    private Long updatedAt;
}
