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
                .eq(TunnelEntity::getTunnelid, tunnelId)
                .last("LIMIT 1"));
        return toDomain(entity);
    }

    @Override
    public Tunnel findByTunnelCode(Long tunnelCode) {
        TunnelEntity entity = tunnelMapper.selectOne(new LambdaQueryWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelcode, tunnelCode)
                .last("LIMIT 1"));
        return toDomain(entity);
    }

    @Override
    public List<Tunnel> findByNamespace(String namespace, String gridname) {
        return tunnelMapper.selectList(new LambdaQueryWrapper<TunnelEntity>()
                        .eq(TunnelEntity::getNamespace, namespace)
                        .eq(TunnelEntity::getDeleted, 0)
                        .eq(!StringUtils.isBlank(gridname), TunnelEntity::getGridname, gridname)
                        .orderByDesc(TunnelEntity::getCreatedAt))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTunnelId(String tunnelId) {
        return tunnelMapper.exists(new LambdaQueryWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelid, tunnelId));
    }

    @Override
    public boolean existsByTunnelCode(Long tunnelCode) {
        return tunnelMapper.exists(new LambdaQueryWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelcode, tunnelCode));
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
                .eq(TunnelEntity::getTunnelid, tunnelId)
                .set(TunnelEntity::getDeleted, 1)
                .set(TunnelEntity::getUpdatedAt, updatedAt));
    }

    @Override
    public void increaseBandwidthUsed(String tunnelId, long usageBytes, long updatedAt) {
        tunnelMapper.update(null, new LambdaUpdateWrapper<TunnelEntity>()
                .eq(TunnelEntity::getTunnelid, tunnelId)
                .setSql("bandwidthused = bandwidthused + " + usageBytes)
                .set(TunnelEntity::getUpdatedAt, updatedAt));
    }

    private Tunnel toDomain(TunnelEntity entity) {
        if (entity == null) {
            return null;
        }
        return Tunnel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .tunnelid(entity.getTunnelid())
                .tunnelcode(entity.getTunnelcode())
                .gridname(entity.getGridname())
                .expiration(entity.getExpiration())
                .namespace(entity.getNamespace())
                .description(entity.getDescription())
                .cluster(entity.getCluster())
                .bandwidthused(entity.getBandwidthused())
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
        entity.setTunnelid(tunnel.getTunnelid());
        entity.setTunnelcode(tunnel.getTunnelcode());
        entity.setGridname(tunnel.getGridname());
        entity.setExpiration(tunnel.getExpiration());
        entity.setNamespace(tunnel.getNamespace());
        entity.setDescription(tunnel.getDescription());
        entity.setCluster(tunnel.getCluster());
        entity.setBandwidthused(tunnel.getBandwidthused());
        entity.setUrl(tunnel.getUrl());
        entity.setType(tunnel.getType());
        entity.setDeleted(tunnel.getDeleted());
        entity.setCreatedAt(tunnel.getCreatedAt());
        entity.setUpdatedAt(tunnel.getUpdatedAt());
        return entity;
    }
}
