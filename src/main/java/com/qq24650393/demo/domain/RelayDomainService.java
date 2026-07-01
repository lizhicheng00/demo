package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.BusinessException;
import com.qq24650393.demo.common.ErrorCode;
import com.qq24650393.demo.web.model.ChangeRelayDomainStatusRequest;
import com.qq24650393.demo.web.model.CreateRelayDomainRequest;
import com.qq24650393.demo.web.model.RelayDomainResponse;
import com.qq24650393.demo.web.model.UpdateRelayDomainRequest;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelayDomainService {

    private final RelayDomainRepository repository;

    public RelayDomainService(RelayDomainRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RelayDomainResponse create(CreateRelayDomainRequest request) {
        if (repository.countByDomain(request.getDomain()) > 0) {
            throw new BusinessException(ErrorCode.RELAY_DOMAIN_EXISTS);
        }
        RelayDomain relayDomain = new RelayDomain();
        relayDomain.setDomain(request.getDomain());
        relayDomain.setTargetUrl(request.getTargetUrl());
        relayDomain.setRemark(request.getRemark());
        relayDomain.setStatus(RelayDomainStatus.ENABLED);
        repository.insert(relayDomain);
        return toResponse(find(relayDomain.getId()));
    }

    @Transactional(readOnly = true)
    public List<RelayDomainResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelayDomainResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public RelayDomainResponse update(Long id, UpdateRelayDomainRequest request) {
        RelayDomain relayDomain = find(id);
        repository.findByDomain(request.getDomain())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.RELAY_DOMAIN_EXISTS);
                });
        relayDomain.setDomain(request.getDomain());
        relayDomain.setTargetUrl(request.getTargetUrl());
        relayDomain.setRemark(request.getRemark());
        repository.update(relayDomain);
        return toResponse(find(id));
    }

    @Transactional
    public RelayDomainResponse changeStatus(Long id, ChangeRelayDomainStatusRequest request) {
        RelayDomain relayDomain = find(id);
        relayDomain.setStatus(RelayDomainStatus.valueOf(request.getStatus().getValue()));
        repository.updateStatus(relayDomain);
        return toResponse(find(id));
    }

    @Transactional
    public void delete(Long id) {
        RelayDomain relayDomain = find(id);
        repository.deleteById(relayDomain.getId());
    }

    private RelayDomain find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELAY_DOMAIN_NOT_FOUND));
    }

    private RelayDomainResponse toResponse(RelayDomain relayDomain) {
        return new RelayDomainResponse()
                .id(relayDomain.getId())
                .domain(relayDomain.getDomain())
                .targetUrl(relayDomain.getTargetUrl())
                .status(com.qq24650393.demo.web.model.RelayDomainStatus.fromValue(relayDomain.getStatus().name()))
                .remark(relayDomain.getRemark())
                .createdAt(relayDomain.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(relayDomain.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }
}
