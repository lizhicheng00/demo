package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class NamespaceService {
    private static final int MAX_NAMESPACE_LENGTH = 128;

    public String requireNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String normalized = namespace.trim();
        if (normalized.length() > MAX_NAMESPACE_LENGTH
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "X-Namespace is invalid");
        }
        return normalized;
    }
}
