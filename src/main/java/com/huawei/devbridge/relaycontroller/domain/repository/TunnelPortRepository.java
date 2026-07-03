package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.TunnelPort;
import java.util.List;

public interface TunnelPortRepository {
    TunnelPort save(TunnelPort tunnelPort);

    List<TunnelPort> findByTunnelCode(Long tunnelCode);

    TunnelPort findByTunnelCodeAndPort(Long tunnelCode, Long port);

    boolean existsByTunnelCodeAndPort(Long tunnelCode, Long port);

    void updateAllowAnonymous(Long tunnelCode, Long port, Boolean allowAnonymous);

    void deleteByTunnelCodeAndPort(Long tunnelCode, Long port);

    void deleteByTunnelCode(Long tunnelCode);
}
