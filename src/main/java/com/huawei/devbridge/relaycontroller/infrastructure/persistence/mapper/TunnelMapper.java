package com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.TunnelEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TunnelMapper extends BaseMapper<TunnelEntity> {
    @Select("""
            SELECT t.*
            FROM tunnel t
            INNER JOIN grid g ON g.grid = t.grid_name
            WHERE t.tunnel_id = #{tunnelId}
              AND g.region = #{region}
              AND t.deleted = 0
            LIMIT 1
            """)
    TunnelEntity selectByTunnelIdAndRegion(
            @Param("tunnelId") String tunnelId,
            @Param("region") String region);

    @Select("""
            <script>
            SELECT t.*
            FROM tunnel t
            INNER JOIN grid g ON g.grid = t.grid_name
            WHERE t.namespace = #{namespace}
              AND g.region = #{region}
              AND t.deleted = 0
              <if test="gridName != null and gridName != ''">
              AND t.grid_name = #{gridName}
              </if>
            ORDER BY t.created_at DESC
            </script>
            """)
    List<TunnelEntity> selectByNamespaceAndRegion(
            @Param("namespace") String namespace,
            @Param("gridName") String gridName,
            @Param("region") String region);

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
