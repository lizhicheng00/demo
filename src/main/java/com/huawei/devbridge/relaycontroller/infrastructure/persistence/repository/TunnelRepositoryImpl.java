package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.huawei.devbridge.relaycontroller.common.util.StringUtils;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.repository.TunnelRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.TunnelEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper.TunnelMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TunnelRepositoryImpl implements TunnelRepository {
    private final TunnelMapper tunnelMapper;

    @Override
    public Tunnel findByTunnelId(String tunnelId) {
        TunnelEntity entity = tunnelMapper.selectOne(new LambdaQueryWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelId, tunnelId)
                .last("LIMIT 1"));
        return toDomain(entity);
    }

    @Override
    public List<Tunnel> findByNamespace(String namespace, String gridName) {
        return tunnelMapper.selectList(new LambdaQueryWrapper<TunnelEntity>()
                        .eq(TunnelEntity::getNamespace, namespace)
                        .eq(TunnelEntity::getDeleted, 0)
                        .eq(!StringUtils.isBlank(gridName), TunnelEntity::getGridName, gridName)
                        .orderByDesc(TunnelEntity::getCreatedAt))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTunnelId(String tunnelId) {
        return tunnelMapper.exists(new LambdaQueryWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelId, tunnelId));
    }

    @Override
    public boolean existsByTunnelCode(Long tunnelCode) {
        return tunnelMapper.exists(new LambdaQueryWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelCode, tunnelCode));
    }

    @Override
    public Tunnel save(Tunnel tunnel) {
        TunnelEntity entity = toEntity(tunnel);
        tunnelMapper.insert(entity);
        tunnel.setId(entity.getId());
        return tunnel;
    }

    @Override
    public void update(Tunnel tunnel) {
        tunnelMapper.updateById(toEntity(tunnel));
    }

    @Override
    public void softDelete(String tunnelId, long updatedAt) {
        tunnelMapper.update(null, new LambdaUpdateWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelId, tunnelId)
                .eq(TunnelEntity::getDeleted, 0)
                .set(TunnelEntity::getDeleted, 1)
                .set(TunnelEntity::getUpdatedAt, updatedAt));
    }

    @Override
    public void softDeleteByNamespace(String namespace, long updatedAt) {
        tunnelMapper.update(null, new LambdaUpdateWrapper<TunnelEntity>()
                .eq(TunnelEntity::getNamespace, namespace)
                .eq(TunnelEntity::getDeleted, 0)
                .set(TunnelEntity::getDeleted, 1)
                .set(TunnelEntity::getUpdatedAt, updatedAt));
    }

    @Override
    public void increaseBandwidthUsed(String tunnelId, long usageBytes, long updatedAt) {
        tunnelMapper.increaseBandwidthUsed(tunnelId, usageBytes, updatedAt);
    }

    private Tunnel toDomain(TunnelEntity entity) {
        if (entity == null) {
            return null;
        }
        return Tunnel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .tunnelId(entity.getTunnelId())
                .tunnelCode(entity.getTunnelCode())
                .gridName(entity.getGridName())
                .expiration(entity.getExpiration())
                .namespace(entity.getNamespace())
                .description(entity.getDescription())
                .cluster(entity.getCluster())
                .bandwidthUsed(entity.getBandwidthUsed())
                .url(entity.getUrl())
                .type(entity.getType())
                .deleted(entity.getDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private TunnelEntity toEntity(Tunnel tunnel) {
        TunnelEntity entity = new TunnelEntity();
        entity.setId(tunnel.getId());
        entity.setName(tunnel.getName());
        entity.setTunnelId(tunnel.getTunnelId());
        entity.setTunnelCode(tunnel.getTunnelCode());
        entity.setGridName(tunnel.getGridName());
        entity.setExpiration(tunnel.getExpiration());
        entity.setNamespace(tunnel.getNamespace());
        entity.setDescription(tunnel.getDescription());
        entity.setCluster(tunnel.getCluster());
        entity.setBandwidthUsed(tunnel.getBandwidthUsed());
        entity.setUrl(tunnel.getUrl());
        entity.setType(tunnel.getType());
        entity.setDeleted(tunnel.getDeleted());
        entity.setCreatedAt(tunnel.getCreatedAt());
        entity.setUpdatedAt(tunnel.getUpdatedAt());
        return entity;
    }
}
