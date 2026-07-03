package com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.TunnelEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface TunnelMapper extends BaseMapper<TunnelEntity> {

    @Update("""
            UPDATE tunnel
            SET bandwidth_used = bandwidth_used + #{usageBytes},
                updated_at = #{updatedAt}
            WHERE tunnel_id = #{tunnelId}
              AND deleted = 0
            """)
    int increaseBandwidthUsed(
            @Param("tunnelId") String tunnelId,
            @Param("usageBytes") long usageBytes,
            @Param("updatedAt") long updatedAt);
}
