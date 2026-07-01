package com.qq24650393.demo.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelayDomainRepository extends JpaRepository<RelayDomain, Long> {

    boolean existsByDomain(String domain);

    Optional<RelayDomain> findByDomain(String domain);

    long countByStatus(RelayDomainStatus status);
}
