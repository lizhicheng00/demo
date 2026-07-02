package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.RelayStatus;

public interface RelayStatusRepository {
    RelayStatus findByTunnelId(String tunnelId);
}
