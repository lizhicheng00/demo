package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class NamespaceService {

    public String resolveNamespace(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return "ns-" + userId.trim();
    }
}
