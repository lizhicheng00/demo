package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import java.util.List;

public interface TunnelRepository {
    Tunnel findByTunnelIdAndRegion(String tunnelId, String region);

    List<Tunnel> findByNamespaceAndRegion(String namespace, String gridName, String region);

    List<Tunnel> findActiveByNamespaceAndRegion(String namespace, String gridName, String region, long now);

    boolean existsByTunnelId(String tunnelId);

    boolean existsByTunnelCode(Long tunnelCode);

    Tunnel save(Tunnel tunnel);

    void update(Tunnel tunnel);

    void softDelete(String tunnelId, long updatedAt);

    void increaseBandwidthUsed(String tunnelId, long usageBytes, long updatedAt);
}
