package com.huawei.devbridge.relaycontroller.application.service;

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

    @Transactional
    public RegisterNodeResponse registerNode(String gridName, RegisterNodeRequest request) {
        ensureGridExists(gridName);
        long now = TimeUtils.nowSeconds();
        NodeRegistry nodeRegistry;
        if (StringUtils.isBlank(request.getNodeId())) {
            nodeRegistry = nodeRegistryRepository.save(NodeRegistry.builder()
                    .gridName(gridName)
                    .ip(request.getIp())
                    .registerTime(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        } else {
            Long id = nodeDomainService.parseNodeId(request.getNodeId());
            nodeRegistry = nodeRegistryRepository.findById(id);
            if (nodeRegistry == null) {
                throw new BizException(ErrorCode.NODE_NOT_FOUND);
            }
            if (!gridName.equals(nodeRegistry.getGridName())) {
                throw new BizException(ErrorCode.NODE_NOT_FOUND);
            }
            nodeRegistry.setIp(request.getIp());
            nodeRegistry.setRegisterTime(now);
            nodeRegistry.setUpdatedAt(now);
            nodeRegistryRepository.update(nodeRegistry);
        }
        List<NodeRegistry> nodes = nodeRegistryRepository.findByGridName(gridName);
        return RegisterNodeResponse.builder()
                .nodeId(nodeDomainService.toNodeId(nodeRegistry.getId()))
                .nodeList(nodes.stream().map(NodeRegistry::getIp).toList())
                .build();
    }

    public NodeInfoResponse getNode(String gridName, String nodeId) {
        ensureGridExists(gridName);
        Long id = nodeDomainService.parseNodeId(nodeId);
        NodeRegistry nodeRegistry = nodeRegistryRepository.findById(id);
        if (nodeRegistry == null || !gridName.equals(nodeRegistry.getGridName())) {
            throw new BizException(ErrorCode.NODE_NOT_FOUND);
        }
        return NodeInfoResponse.builder().ip(nodeRegistry.getIp()).build();
    }

    private void ensureGridExists(String gridName) {
        if (!gridRepository.existsByGridName(gridName)) {
            throw new BizException(ErrorCode.GRID_NOT_FOUND);
        }
    }
}
