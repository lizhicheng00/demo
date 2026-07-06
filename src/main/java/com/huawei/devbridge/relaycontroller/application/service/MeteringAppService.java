package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
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
    private final LocalGridService localGridService;
    private final TunnelRepository tunnelRepository;
    private final MeteringRepository meteringRepository;
    private final TunnelDomainService tunnelDomainService;
    private final RelayProperties relayProperties;

    @Transactional
    public MeteringReportResponse report(String gridName, MeteringReportRequest request) {
        localGridService.requireLocalGrid(gridName);
        Tunnel tunnel = tunnelRepository.findByTunnelIdAndRegion(request.getTunnelId(), relayProperties.getRegion());
        tunnelDomainService.assertInGrid(tunnel, gridName, ErrorCode.METERING_REPORT_FAILED);
        if (!request.getTunnelCode().equals(tunnel.getTunnelCode())) {
            throw new BizException(ErrorCode.METERING_REPORT_FAILED, "metering tunnel mismatch");
        }
        long now = TimeUtils.nowSeconds();
        meteringRepository.save(Metering.builder()
                .gridName(gridName)
                .tunnelCode(request.getTunnelCode())
                .tunnelId(request.getTunnelId())
                .usageBytes(request.getUsage())
                .reportedAt(now)
                .createdAt(now)
                .build());
        tunnelRepository.increaseBandwidthUsed(request.getTunnelId(), request.getUsage(), now);
        log.info("Metering accepted: tunnelId={}, tunnelCode={}, gridName={}, usageBytes={}",
                request.getTunnelId(), request.getTunnelCode(), gridName, request.getUsage());
        return MeteringReportResponse.builder().accepted(true).build();
    }
}
