package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.assembler.NodeAssembler;
import com.huawei.devbridge.relaycontroller.application.service.NodeAppService;
import com.huawei.devbridge.relaycontroller.domain.model.NodeRegistry;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.NodeRegistryRepository;
import com.huawei.devbridge.relaycontroller.domain.service.NodeDomainService;
import com.huawei.devbridge.relaycontroller.interfaces.request.RegisterNodeRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.RegisterNodeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeAppServiceTest {
    @Mock
    private GridRepository gridRepository;
    @Mock
    private NodeRegistryRepository nodeRegistryRepository;

    @Test
    void registerNewNodeUsesDatabaseIdAsHexNodeId() {
        NodeAppService service = new NodeAppService(
                gridRepository,
                nodeRegistryRepository,
                new NodeDomainService(),
                new NodeAssembler());
        RegisterNodeRequest request = new RegisterNodeRequest();
        request.setIp("10.0.1.23");

        when(gridRepository.existsByGridName("grid-a")).thenReturn(true);
        when(nodeRegistryRepository.save(any(NodeRegistry.class))).thenAnswer(invocation -> {
            NodeRegistry node = invocation.getArgument(0);
            node.setId(15L);
            return node;
        });
        when(nodeRegistryRepository.findByGridName("grid-a"))
                .thenReturn(List.of(NodeRegistry.builder().id(15L).ip("10.0.1.23").build()));

        RegisterNodeResponse response = service.registerNode("grid-a", request);

        assertThat(response.getNodeId()).isEqualTo("000f");
        assertThat(response.getNodeList()).containsExactly("10.0.1.23");
    }
}
