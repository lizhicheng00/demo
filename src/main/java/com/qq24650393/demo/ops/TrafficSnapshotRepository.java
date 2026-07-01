package com.qq24650393.demo.ops;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrafficSnapshotRepository extends JpaRepository<TrafficSnapshot, Long> {

    List<TrafficSnapshot> findTop100ByCapturedAtAfterOrderByCapturedAtDesc(Instant capturedAt);

    @Query("""
            select coalesce(sum(t.inboundBytes), 0),
                   coalesce(sum(t.outboundBytes), 0),
                   coalesce(sum(t.activeConnections), 0)
            from TrafficSnapshot t
            where t.capturedAt >= :since
            """)
    Object[] sumSince(@Param("since") Instant since);
}
