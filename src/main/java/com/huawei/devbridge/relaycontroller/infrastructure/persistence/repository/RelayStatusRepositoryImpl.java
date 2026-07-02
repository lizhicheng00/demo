package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.huawei.devbridge.relaycontroller.domain.model.RelayStatus;
import com.huawei.devbridge.relaycontroller.domain.repository.RelayStatusRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.redis.RelayStatusCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RelayStatusRepositoryImpl implements RelayStatusRepository {
    private final RelayStatusCache relayStatusCache;

    @Override
    public RelayStatus findByTunnelId(String tunnelId) {
        return relayStatusCache.get(tunnelId);
    }
}
