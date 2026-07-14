package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Cluster;
import com.huawei.devbridge.relaycontroller.domain.model.Metering;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.MeteringRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeteringAppService {
    private final LocalClusterService localClusterService;
    private final TunnelRepository tunnelRepository;
    private final MeteringRepository meteringRepository;
    private final TunnelDomainService tunnelDomainService;
    private final RelayProperties relayProperties;

    @Transactional
    public MeteringReportResponse report(String clusterId, MeteringReportRequest request) {
        Cluster cluster = localClusterService.requireLocalCluster(clusterId);
        Tunnel tunnel = tunnelRepository.findByTunnelIdAndRegion(request.getTunnelId(), relayProperties.getRegion());
        tunnelDomainService.assertInClusterAndNotExpired(
                tunnel, cluster.getClusterId(), ErrorCode.METERING_REPORT_FAILED);
        if (!request.getTunnelCode().equals(tunnel.getTunnelCode())) {
            throw new BizException(ErrorCode.METERING_REPORT_FAILED, "metering tunnel mismatch");
        }
        long now = TimeUtils.nowSeconds();
        if (!tunnelRepository.increaseBandwidthUsed(
                request.getTunnelId(), relayProperties.getRegion(), request.getUsage(), now)) {
            throw new BizException(ErrorCode.METERING_REPORT_FAILED);
        }
        meteringRepository.save(Metering.builder()
                .clusterId(cluster.getClusterId())
                .tunnelCode(request.getTunnelCode())
                .tunnelId(request.getTunnelId())
                .usageBytes(request.getUsage())
                .reportedAt(now)
                .createdAt(now)
                .build());
        log.info("Metering accepted: tunnelId={}, tunnelCode={}, clusterId={}, usageBytes={}",
                request.getTunnelId(), request.getTunnelCode(), cluster.getClusterId(), request.getUsage());
        return MeteringReportResponse.builder().accepted(true).build();
    }
}
