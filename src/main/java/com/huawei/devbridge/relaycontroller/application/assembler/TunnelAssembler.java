package com.huawei.devbridge.relaycontroller.application.assembler;

import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelType;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.JwtResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;

public final class TunnelAssembler {
    private TunnelAssembler() {
    }

    public static CreateTunnelResponse toCreateResponse(Tunnel tunnel, String token, long expiresIn) {
        return CreateTunnelResponse.builder()
                .name(tunnel.getName())
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
                .jwt(jwtResponse(token, expiresIn))
                .build();
    }

    public static TunnelDetailResponse toDetailResponse(Tunnel tunnel, String token, long expiresIn) {
        return TunnelDetailResponse.builder()
                .name(tunnel.getName())
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
                .jwt(jwtResponse(token, expiresIn))
                .build();
    }

    public static TunnelListItemResponse toListItem(Tunnel tunnel) {
        return TunnelListItemResponse.builder()
                .tunnelId(tunnel.getTunnelId())
                .tunnelCode(tunnel.getTunnelCode())
                .gridName(tunnel.getGridName())
                .name(tunnel.getName())
                .description(tunnel.getDescription())
                .expiration(tunnel.getExpiration())
                .created(tunnel.getCreatedAt())
                .url(tunnel.getUrl())
                .build();
    }

    private static String typeValue(Tunnel tunnel) {
        TunnelType type = tunnel.getType();
        return type == null ? null : type.value();
    }

    private static JwtResponse jwtResponse(String token, long expiresIn) {
        return JwtResponse.builder()
                .tokenType("TOKEN")
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }
}
