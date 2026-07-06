package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import java.util.List;

public interface TunnelRepository {
    Tunnel findByTunnelId(String tunnelId);

    List<Tunnel> findByNamespace(String namespace, String gridName);

    boolean existsByTunnelId(String tunnelId);

    boolean existsByTunnelCode(Long tunnelCode);

    Tunnel save(Tunnel tunnel);

    void update(Tunnel tunnel);

    void softDelete(String tunnelId, long updatedAt);

    void increaseBandwidthUsed(String tunnelId, long usageBytes, long updatedAt);
}
