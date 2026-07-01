package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.BusinessException;
import com.qq24650393.demo.common.ErrorCode;
import java.util.List;
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
        RelayDomain relayDomain = relayDomainRepository.findById(request.relayDomainId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RELAY_DOMAIN_NOT_FOUND));
        Node node = null;
        if (request.nodeCode() != null && !request.nodeCode().isBlank()) {
            node = nodeRepository.findByNodeCode(request.nodeCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NODE_NOT_FOUND));
        }
        ListeningConfig config = new ListeningConfig();
        config.setRelayDomain(relayDomain);
        config.setNode(node);
        config.setListenPort(request.listenPort());
        config.setProtocol(request.protocol());
        config.setStatus(request.status() == null ? ListeningStatus.ENABLED : request.status());
        return ListeningResponse.from(repository.save(config));
    }

    @Transactional(readOnly = true)
    public List<ListeningResponse> sync(String nodeCode) {
        nodeService.findByCode(nodeCode);
        return repository.findSyncable(nodeCode, ListeningStatus.ENABLED, RelayDomainStatus.ENABLED).stream()
                .map(ListeningResponse::from)
                .toList();
    }

    @Transactional
    public ListeningResponse changeStatus(Long id, ChangeListeningStatusRequest request) {
        ListeningConfig config = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.LISTENING_NOT_FOUND));
        config.setStatus(request.status());
        return ListeningResponse.from(config);
    }
}
