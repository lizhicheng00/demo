package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class TunnelDomainService {

    public void assertActive(Tunnel tunnel) {
        if (tunnel == null || Integer.valueOf(1).equals(tunnel.getDeleted())) {
            throw new BizException(ErrorCode.TUNNEL_NOT_FOUND);
        }
    }

    public void assertOwnedBy(Tunnel tunnel, String namespace) {
        assertActive(tunnel);
        if (!namespace.equals(tunnel.getNamespace())) {
            throw new BizException(ErrorCode.TUNNEL_ACCESS_DENIED);
        }
    }

    public void assertNotExpired(Tunnel tunnel) {
        assertActive(tunnel);
        if (tunnel.getExpiration() != null && tunnel.getExpiration() <= TimeUtils.nowSeconds()) {
            throw new BizException(ErrorCode.TUNNEL_EXPIRED);
        }
    }

    public void assertInGrid(Tunnel tunnel, String gridName, ErrorCode mismatchErrorCode) {
        assertActive(tunnel);
        if (!Objects.equals(gridName, tunnel.getGridName())) {
            throw new BizException(mismatchErrorCode);
        }
    }

    public String normalizeType(String type) {
        if (StringUtils.isBlank(type)) {
            return TunnelType.BRIDGE.value();
        }
        String normalizedType = type.trim().toLowerCase();
        if (!TunnelType.supports(normalizedType)) {
            throw new BizException(ErrorCode.TUNNEL_TYPE_INVALID);
        }
        return normalizedType;
    }
}
