package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.BusinessException;
import com.qq24650393.demo.common.ErrorCode;
import com.qq24650393.demo.web.model.ChangeListeningStatusRequest;
import com.qq24650393.demo.web.model.CreateListeningRequest;
import com.qq24650393.demo.web.model.ListeningResponse;
import java.util.List;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListeningService {

    private final ListeningConfigRepository repository;
    private final RelayDomainRepository relayDomainRepository;
    private final NodeRepository nodeRepository;
    private final NodeService nodeService;

    public ListeningService(
            ListeningConfigRepository repository,
            RelayDomainRepository relayDomainRepository,
            NodeRepository nodeRepository,
            NodeService nodeService) {
        this.repository = repository;
        this.relayDomainRepository = relayDomainRepository;
        this.nodeRepository = nodeRepository;
        this.nodeService = nodeService;
    }

    @Transactional
    public ListeningResponse create(CreateListeningRequest request) {
        RelayDomain relayDomain = relayDomainRepository.findById(request.getRelayDomainId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RELAY_DOMAIN_NOT_FOUND));
        Node node = null;
        if (request.getNodeCode() != null && !request.getNodeCode().isBlank()) {
            node = nodeRepository.findByNodeCode(request.getNodeCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NODE_NOT_FOUND));
        }
        ListeningConfig config = new ListeningConfig();
        config.setRelayDomainId(relayDomain.getId());
        config.setNodeId(node == null ? null : node.getId());
        config.setListenPort(request.getListenPort());
        config.setProtocol(ListeningProtocol.valueOf(request.getProtocol().getValue()));
        config.setStatus(request.getStatus() == null
                ? ListeningStatus.ENABLED
                : ListeningStatus.valueOf(request.getStatus().getValue()));
        config.setVersion(1L);
        repository.insert(config);
        return toResponse(repository.findById(config.getId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<ListeningResponse> sync(String nodeCode) {
        nodeService.findByCode(nodeCode);
        return repository.findSyncable(nodeCode, ListeningStatus.ENABLED, RelayDomainStatus.ENABLED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ListeningResponse changeStatus(Long id, ChangeListeningStatusRequest request) {
        ListeningConfig config = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.LISTENING_NOT_FOUND));
        config.setStatus(ListeningStatus.valueOf(request.getStatus().getValue()));
        repository.updateStatus(config);
        return toResponse(repository.findById(id).orElseThrow());
    }

    private ListeningResponse toResponse(ListeningConfig config) {
        return new ListeningResponse()
                .id(config.getId())
                .relayDomainId(config.getRelayDomainId())
                .domain(config.getDomain())
                .targetUrl(config.getTargetUrl())
                .nodeCode(config.getNodeCode())
                .listenPort(config.getListenPort())
                .protocol(com.qq24650393.demo.web.model.ListeningProtocol.fromValue(config.getProtocol().name()))
                .status(com.qq24650393.demo.web.model.ListeningStatus.fromValue(config.getStatus().name()))
                .version(config.getVersion())
                .updatedAt(config.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }
}
