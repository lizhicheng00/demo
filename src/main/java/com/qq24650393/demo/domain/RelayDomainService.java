package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.BusinessException;
import com.qq24650393.demo.common.ErrorCode;
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
        if (repository.existsByDomain(request.domain())) {
            throw new BusinessException(ErrorCode.RELAY_DOMAIN_EXISTS);
        }
        RelayDomain relayDomain = new RelayDomain();
        relayDomain.setDomain(request.domain());
        relayDomain.setTargetUrl(request.targetUrl());
        relayDomain.setRemark(request.remark());
        return RelayDomainResponse.from(repository.save(relayDomain));
    }

    @Transactional(readOnly = true)
    public List<RelayDomainResponse> list() {
        return repository.findAll().stream()
                .map(RelayDomainResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelayDomainResponse get(Long id) {
        return RelayDomainResponse.from(find(id));
    }

    @Transactional
    public RelayDomainResponse update(Long id, UpdateRelayDomainRequest request) {
        RelayDomain relayDomain = find(id);
        repository.findByDomain(request.domain())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.RELAY_DOMAIN_EXISTS);
                });
        relayDomain.setDomain(request.domain());
        relayDomain.setTargetUrl(request.targetUrl());
        relayDomain.setRemark(request.remark());
        return RelayDomainResponse.from(relayDomain);
    }

    @Transactional
    public RelayDomainResponse changeStatus(Long id, ChangeRelayDomainStatusRequest request) {
        RelayDomain relayDomain = find(id);
        relayDomain.setStatus(request.status());
        return RelayDomainResponse.from(relayDomain);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private RelayDomain find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELAY_DOMAIN_NOT_FOUND));
    }
}
