package com.qq24650393.demo.ops;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TrafficSnapshotRepository {

    @Select("""
            select t.id, t.node_id, n.node_code, t.captured_at, t.inbound_bytes, t.outbound_bytes,
                   t.active_connections
            from traffic_snapshots t
            left join nodes n on n.id = t.node_id
            where t.captured_at > #{capturedAt}
            order by t.captured_at desc
            limit 100
            """)
    List<TrafficSnapshot> findTop100ByCapturedAtAfterOrderByCapturedAtDesc(Instant capturedAt);

    @Select("""
            select coalesce(sum(inbound_bytes), 0) as inboundBytes,
                   coalesce(sum(outbound_bytes), 0) as outboundBytes,
                   coalesce(sum(active_connections), 0) as activeConnections
            from traffic_snapshots
            where captured_at >= #{since}
            """)
    TrafficSummary sumSince(Instant since);
}
