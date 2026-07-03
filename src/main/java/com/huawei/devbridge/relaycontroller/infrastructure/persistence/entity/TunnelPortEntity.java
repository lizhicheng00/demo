package com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tunnel_port")
public class TunnelPortEntity {
    @TableId(value = "_id", type = IdType.AUTO)
    private Long id;
    private Long tunnelCode;
    private Long port;
    private Boolean allowAnonymous;
}
