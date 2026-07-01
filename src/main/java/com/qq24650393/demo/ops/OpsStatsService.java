package com.qq24650393.demo.ops;

import com.qq24650393.demo.domain.ListeningConfigRepository;
import com.qq24650393.demo.domain.ListeningStatus;
import com.qq24650393.demo.domain.NodeRepository;
import com.qq24650393.demo.domain.NodeStatus;
import com.qq24650393.demo.domain.RelayDomainRepository;
import com.qq24650393.demo.domain.RelayDomainStatus;
import com.qq24650393.demo.web.model.OpsOverviewResponse;
import com.qq24650393.demo.web.model.TrafficPointResponse;
import java.time.Instant;
import java.time.ZoneOffset;
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
        TrafficSummary sums = trafficSnapshotRepository.sumSince(since);
        return new OpsOverviewResponse()
                .totalRelayDomains(relayDomainRepository.count())
                .enabledRelayDomains(relayDomainRepository.countByStatus(RelayDomainStatus.ENABLED))
                .totalNodes(nodeRepository.count())
                .activeNodes(nodeRepository.countByStatus(NodeStatus.ACTIVE))
                .enabledListenings(listeningConfigRepository.countByStatus(ListeningStatus.ENABLED))
                .inboundBytes24h(sums.getInboundBytes())
                .outboundBytes24h(sums.getOutboundBytes())
                .activeConnections24h(sums.getActiveConnections());
    }

    @Transactional(readOnly = true)
    public List<TrafficPointResponse> traffic() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        return trafficSnapshotRepository.findTop100ByCapturedAtAfterOrderByCapturedAtDesc(since).stream()
                .map(this::toResponse)
                .toList();
    }

    private TrafficPointResponse toResponse(TrafficSnapshot snapshot) {
        return new TrafficPointResponse()
                .capturedAt(snapshot.getCapturedAt().atOffset(ZoneOffset.UTC))
                .nodeCode(snapshot.getNodeCode())
                .inboundBytes(snapshot.getInboundBytes())
                .outboundBytes(snapshot.getOutboundBytes())
                .activeConnections(snapshot.getActiveConnections());
    }
}
