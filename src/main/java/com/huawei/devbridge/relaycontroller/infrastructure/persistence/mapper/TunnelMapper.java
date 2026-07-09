package com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.TunnelEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TunnelMapper extends BaseMapper<TunnelEntity> {
    TunnelEntity selectByTunnelIdAndRegion(
            @Param("tunnelId") String tunnelId,
            @Param("region") String region);

    List<TunnelEntity> selectByNamespaceAndRegion(
            @Param("namespace") String namespace,
            @Param("region") String region);

    List<TunnelEntity> selectActiveByNamespaceAndRegion(
            @Param("namespace") String namespace,
            @Param("gridName") String gridName,
            @Param("region") String region,
            @Param("now") long now);

    long countActiveByNamespaceAndRegion(
            @Param("namespace") String namespace,
            @Param("region") String region,
            @Param("now") long now);

    int increaseBandwidthUsed(
            @Param("tunnelId") String tunnelId,
            @Param("region") String region,
            @Param("usageBytes") long usageBytes,
            @Param("updatedAt") long updatedAt);
}
