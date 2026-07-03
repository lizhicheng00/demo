package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.MeteringAppService;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.MeteringRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeteringAppServiceTest {
    @Mock
    private GridRepository gridRepository;
    @Mock
    private TunnelRepository tunnelRepository;
    @Mock
    private MeteringRepository meteringRepository;

    @Test
    void reportWritesMeteringAndIncreasesTunnelBandwidth() {
        MeteringAppService service = new MeteringAppService(
                gridRepository,
                tunnelRepository,
                meteringRepository);
        MeteringReportRequest request = new MeteringReportRequest();
        request.setTunnelId("000001e240");
        request.setTunnelCode(123456L);
        request.setUsage(1024L);

        when(gridRepository.existsByGridName("grid-a")).thenReturn(true);
        when(tunnelRepository.findByTunnelId("000001e240")).thenReturn(Tunnel.builder()
                .tunnelId("000001e240")
                .tunnelCode(123456L)
                .gridName("grid-a")
                .deleted(0)
                .build());

        MeteringReportResponse response = service.report("grid-a", request);

        assertThat(response.getAccepted()).isTrue();
        verify(meteringRepository).save(ArgumentMatchers.argThat(metering -> metering.getUsageBytes().equals(1024L)));
        verify(tunnelRepository).increaseBandwidthUsed(ArgumentMatchers.eq("000001e240"), ArgumentMatchers.eq(1024L), ArgumentMatchers.anyLong());
    }
}
