package com.huawei.devbridge.relaycontroller.application.assembler;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
import org.springframework.stereotype.Component;

@Component
public class TunnelAssembler {

    public TunnelListItemResponse toListItem(Tunnel tunnel) {
        return TunnelListItemResponse.builder()
                .name(tunnel.getName())
                .description(tunnel.getDescription())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .build();
    }

    public TunnelDetailResponse toDetail(Tunnel tunnel, String accessToken) {
        return TunnelDetailResponse.builder()
                .name(tunnel.getName())
                .id(tunnel.getTunnelid())
                .tunnelId(tunnel.getTunnelid())
                .tunnelCode(tunnel.getTunnelcode())
                .gridname(tunnel.getGridname())
                .cluster(tunnel.getCluster())
                .description(tunnel.getDescription())
                .bandwidthUsed(tunnel.getBandwidthused())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .type(tunnel.getType())
                .accessToken(accessToken)
                .build();
    }

    public CreateTunnelResponse toCreateResponse(Tunnel tunnel, String accessToken) {
        return CreateTunnelResponse.builder()
                .name(tunnel.getName())
                .id(tunnel.getTunnelid())
                .tunnelId(tunnel.getTunnelid())
                .tunnelCode(tunnel.getTunnelcode())
                .gridname(tunnel.getGridname())
                .cluster(tunnel.getCluster())
                .description(tunnel.getDescription())
                .bandwidthUsed(tunnel.getBandwidthused())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .type(tunnel.getType())
                .accessToken(accessToken)
                .build();
    }
}
