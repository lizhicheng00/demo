package com.huawei.devbridge.relaycontroller.application.assembler;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelResponse;

public final class TunnelAssembler {
    private TunnelAssembler() {
    }

    public static TunnelResponse toResponse(Tunnel tunnel) {
        return TunnelResponse.builder()
                .name(tunnel.getName())
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .clusterId(tunnel.getClusterId())
                .description(tunnel.getDescription())
                .bandwidthUsed(tunnel.getBandwidthUsed())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .type(tunnel.getType())
                .build();
    }

    public static TunnelListItemResponse toListItem(Tunnel tunnel) {
        return TunnelListItemResponse.builder()
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .clusterId(tunnel.getClusterId())
                .name(tunnel.getName())
                .description(tunnel.getDescription())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .portCount(tunnel.getPortCount() == null ? 0L : tunnel.getPortCount())
                .build();
    }
}
