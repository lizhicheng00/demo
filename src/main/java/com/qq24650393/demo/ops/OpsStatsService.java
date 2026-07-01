package com.qq24650393.demo.ops;

import com.qq24650393.demo.domain.ListeningConfigRepository;
import com.qq24650393.demo.domain.ListeningStatus;
import com.qq24650393.demo.domain.NodeRepository;
import com.qq24650393.demo.domain.NodeStatus;
import com.qq24650393.demo.domain.RelayDomainRepository;
import com.qq24650393.demo.domain.RelayDomainStatus;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpsStatsService {

    private final RelayDomainRepository relayDomainRepository;
    private final NodeRepository nodeRepository;
    private final ListeningConfigRepository listeningConfigRepository;
    private final TrafficSnapshotRepository trafficSnapshotRepository;

    public OpsStatsService(
            RelayDomainRepository relayDomainRepository,
            NodeRepository nodeRepository,
            ListeningConfigRepository listeningConfigRepository,
            TrafficSnapshotRepository trafficSnapshotRepository) {
        this.relayDomainRepository = relayDomainRepository;
        this.nodeRepository = nodeRepository;
        this.listeningConfigRepository = listeningConfigRepository;
        this.trafficSnapshotRepository = trafficSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public OpsOverviewResponse overview() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        Object[] sums = trafficSnapshotRepository.sumSince(since);
        Object[] values = sums.length == 1 && sums[0] instanceof Object[] nested ? nested : sums;
        return new OpsOverviewResponse(
                relayDomainRepository.count(),
                relayDomainRepository.countByStatus(RelayDomainStatus.ENABLED),
                nodeRepository.count(),
                nodeRepository.countByStatus(NodeStatus.ACTIVE),
                listeningConfigRepository.countByStatus(ListeningStatus.ENABLED),
                toLong(values[0]),
                toLong(values[1]),
                toLong(values[2]));
    }

    @Transactional(readOnly = true)
    public List<TrafficPointResponse> traffic() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        return trafficSnapshotRepository.findTop100ByCapturedAtAfterOrderByCapturedAtDesc(since).stream()
                .map(TrafficPointResponse::from)
                .toList();
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.longValue();
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.longValue();
        }
        return 0L;
    }
}
