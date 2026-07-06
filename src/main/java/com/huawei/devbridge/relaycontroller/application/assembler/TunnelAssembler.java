package com.huawei.devbridge.relaycontroller.application.assembler;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;

public final class TunnelAssembler {
    private TunnelAssembler() {
    }

    public static CreateTunnelResponse toCreateResponse(Tunnel tunnel) {
        return CreateTunnelResponse.builder()
                .name(tunnel.getName())
                .id(tunnel.getTunnelId())
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .gridName(tunnel.getGridName())
                .cluster(tunnel.getCluster())
                .description(tunnel.getDescription())
                .bandwidthUsed(tunnel.getBandwidthUsed())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .type(typeValue(tunnel))
                .build();
    }

    public static TunnelDetailResponse toDetailResponse(Tunnel tunnel) {
        return TunnelDetailResponse.builder()
                .name(tunnel.getName())
                .id(tunnel.getTunnelId())
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .gridName(tunnel.getGridName())
                .cluster(tunnel.getCluster())
                .description(tunnel.getDescription())
                .bandwidthUsed(tunnel.getBandwidthUsed())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .type(typeValue(tunnel))
                .build();
    }

    public static TunnelListItemResponse toListItem(Tunnel tunnel) {
        return TunnelListItemResponse.builder()
                .name(tunnel.getName())
                .description(tunnel.getDescription())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .build();
    }

    private static String typeValue(Tunnel tunnel) {
        TunnelType type = tunnel.getType();
        return type == null ? null : type.value();
    }
}
