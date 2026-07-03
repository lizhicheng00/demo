package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class TunnelPortDomainService {
    private static final long MIN_PORT = 1L;
    private static final long MAX_PORT = 65535L;

    public void validatePort(Long port) {
        if (port == null || port < MIN_PORT || port > MAX_PORT) {
            throw new BizException(ErrorCode.TUNNEL_PORT_INVALID);
        }
    }

    public void validateAllowAnonymous(Boolean allowAnonymous) {
        if (allowAnonymous == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
    }
}
