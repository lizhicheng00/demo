package com.huawei.devbridge.relaycontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huawei.devbridge.relaycontroller.application.service.LocalClusterService;
import com.huawei.devbridge.relaycontroller.application.service.MeteringAppService;
import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.Cluster;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.ClusterRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.MeteringRepository;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.domain.service.TunnelDomainService;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
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
    private ClusterRepository clusterRepository;
    @Mock
    private TunnelRepository tunnelRepository;
    @Mock
    private MeteringRepository meteringRepository;

    @Test
    void reportWritesMeteringAndIncreasesTunnelBandwidth() {
        MeteringAppService service = newService();
        MeteringReportRequest request = new MeteringReportRequest();
        request.setTunnelId("aaaadysa");
        request.setTunnelCode(123456L);
        request.setUsage(1024L);

        when(clusterRepository.findByClusterIdAndRegion("cluster-a", "region-a"))
                .thenReturn(Cluster.builder().clusterId("cluster-a").region("region-a").build());
        when(tunnelRepository.findByTunnelIdAndRegion("aaaadysa", "region-a")).thenReturn(Tunnel.builder()
                .tunnelId("aaaadysa")
                .tunnelCode(123456L)
                .clusterId("cluster-a")
                .deleted(0)
                .build());

        MeteringReportResponse response = service.report("cluster-a", request);

        assertThat(response.getAccepted()).isTrue();
        verify(meteringRepository).save(ArgumentMatchers.argThat(metering -> metering.getUsageBytes().equals(1024L)));
        verify(tunnelRepository).increaseBandwidthUsed(ArgumentMatchers.eq("aaaadysa"), ArgumentMatchers.eq("region-a"),
                ArgumentMatchers.eq(1024L), ArgumentMatchers.anyLong());
    }

    @Test
    void reportRejectsClusterOutsideLocalRegion() {
        MeteringAppService service = newService();
        MeteringReportRequest request = new MeteringReportRequest();
        request.setTunnelId("aaaadysa");
        request.setTunnelCode(123456L);
        request.setUsage(1024L);

        assertThatThrownBy(() -> service.report("cluster-b", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CLUSTER_NOT_FOUND);
    }

    private MeteringAppService newService() {
        RelayProperties properties = new RelayProperties();
        return new MeteringAppService(
                new LocalClusterService(clusterRepository, properties),
                tunnelRepository,
                meteringRepository,
                new TunnelDomainService(),
                properties);
    }
}
