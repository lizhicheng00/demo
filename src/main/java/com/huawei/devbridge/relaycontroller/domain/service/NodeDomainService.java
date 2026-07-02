package com.huawei.devbridge.relaycontroller.domain.service;

import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.HexUtils;
import org.springframework.stereotype.Service;

@Service
public class NodeDomainService {

    public String toNodeId(Long id) {
        return HexUtils.toFixedWidthHex(id, 4);
    }

    public Long parseNodeId(String nodeId) {
        return HexUtils.parseUnsignedLong(nodeId, ErrorCode.NODE_ID_INVALID);
    }

    public boolean fitsLegacy16BitNodeId(Long id) {
        return id != null && id >= 0 && id <= 0xffff;
    }
}
