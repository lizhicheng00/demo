package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.application.assembler.NodeAssembler;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.NodeRegistry;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.NodeRegistryRepository;
import com.huawei.devbridge.relaycontroller.domain.service.NodeDomainService;
import com.huawei.devbridge.relaycontroller.interfaces.request.RegisterNodeRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.NodeInfoResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NodeAppService {
    private final GridRepository gridRepository;
    private final NodeRegistryRepository nodeRegistryRepository;
    private final NodeDomainService nodeDomainService;
    private final NodeAssembler nodeAssembler;

    @Transactional
    public RegisterNodeResponse registerNode(String gridname, RegisterNodeRequest request) {
        ensureGridExists(gridname);
        long now = TimeUtils.nowSeconds();
        NodeRegistry nodeRegistry;
        if (StringUtils.isBlank(request.getNodeId())) {
            nodeRegistry = nodeRegistryRepository.save(NodeRegistry.builder()
                    .gridname(gridname)
                    .ip(request.getIp())
                    .registertime(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        } else {
            Long id = nodeDomainService.parseNodeId(request.getNodeId());
            nodeRegistry = nodeRegistryRepository.findById(id);
            if (nodeRegistry == null) {
                throw new BizException(ErrorCode.NODE_NOT_FOUND);
            }
            if (!gridname.equals(nodeRegistry.getGridname())) {
                throw new BizException(ErrorCode.NODE_NOT_FOUND);
            }
            nodeRegistry.setIp(request.getIp());
            nodeRegistry.setRegistertime(now);
            nodeRegistry.setUpdatedAt(now);
            nodeRegistryRepository.update(nodeRegistry);
        }
        List<NodeRegistry> nodes = nodeRegistryRepository.findByGridName(gridname);
        return nodeAssembler.toRegisterResponse(nodeDomainService.toNodeId(nodeRegistry.getId()), nodes);
    }

    public NodeInfoResponse getNode(String gridname, String nodeId) {
        ensureGridExists(gridname);
        Long id = nodeDomainService.parseNodeId(nodeId);
        NodeRegistry nodeRegistry = nodeRegistryRepository.findById(id);
        if (nodeRegistry == null || !gridname.equals(nodeRegistry.getGridname())) {
            throw new BizException(ErrorCode.NODE_NOT_FOUND);
        }
        return nodeAssembler.toNodeInfo(nodeRegistry);
    }

    private void ensureGridExists(String gridname) {
        if (!gridRepository.existsByGridName(gridname)) {
            throw new BizException(ErrorCode.GRID_NOT_FOUND);
        }
    }
}
