package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.util.TimeUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Metering;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.MeteringRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeteringAppService {
    private final GridRepository gridRepository;
    private final TunnelRepository tunnelRepository;
    private final MeteringRepository meteringRepository;

    @Transactional
    public MeteringReportResponse report(String gridName, MeteringReportRequest request) {
        if (!gridRepository.existsByGridName(gridName)) {
            throw new BizException(ErrorCode.GRID_NOT_FOUND);
        }
        Tunnel tunnel = tunnelRepository.findByTunnelId(request.getTunnelId());
        if (tunnel == null || Integer.valueOf(1).equals(tunnel.getDeleted())) {
            throw new BizException(ErrorCode.TUNNEL_NOT_FOUND);
        }
        if (!gridName.equals(tunnel.getGridName()) || !request.getTunnelCode().equals(tunnel.getTunnelCode())) {
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
        return MeteringReportResponse.builder().accepted(true).build();
    }
}
