package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.validation.IdentifierValidator;
import org.springframework.stereotype.Service;

@Service
public class NamespaceService {
    public String requireNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (!IdentifierValidator.isValid(namespace)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "X-Namespace is invalid");
        }
        return namespace;
    }
}
