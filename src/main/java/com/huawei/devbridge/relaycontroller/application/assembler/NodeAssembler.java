package com.huawei.devbridge.relaycontroller.application.assembler;

import com.huawei.devbridge.relaycontroller.domain.model.NodeRegistry;
import com.huawei.devbridge.relaycontroller.interfaces.response.NodeInfoResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NodeAssembler {

    public RegisterNodeResponse toRegisterResponse(String nodeId, List<NodeRegistry> nodes) {
        return RegisterNodeResponse.builder()
                .nodeId(nodeId)
                .nodeList(nodes.stream().map(NodeRegistry::getIp).toList())
                .build();
    }

    public NodeInfoResponse toNodeInfo(NodeRegistry nodeRegistry) {
        return NodeInfoResponse.builder()
                .ip(nodeRegistry.getIp())
                .build();
    }
}
