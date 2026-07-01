package com.qq24650393.demo.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListeningConfigRepository extends JpaRepository<ListeningConfig, Long> {

    long countByStatus(ListeningStatus status);

    @Query("""
            select l from ListeningConfig l
            join fetch l.relayDomain d
            left join fetch l.node n
            where l.status = :status
              and d.status = :domainStatus
              and (n.nodeCode = :nodeCode or n is null)
            order by l.version desc
            """)
    List<ListeningConfig> findSyncable(
            @Param("nodeCode") String nodeCode,
            @Param("status") ListeningStatus status,
            @Param("domainStatus") RelayDomainStatus domainStatus);
}
