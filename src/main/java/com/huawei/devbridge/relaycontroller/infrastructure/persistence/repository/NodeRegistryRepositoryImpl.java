package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huawei.devbridge.relaycontroller.domain.model.NodeRegistry;
import com.huawei.devbridge.relaycontroller.domain.repository.NodeRegistryRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.NodeRegistryEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper.NodeRegistryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NodeRegistryRepositoryImpl implements NodeRegistryRepository {
    private final NodeRegistryMapper nodeRegistryMapper;

    @Override
    public NodeRegistry save(NodeRegistry nodeRegistry) {
        NodeRegistryEntity entity = toEntity(nodeRegistry);
        nodeRegistryMapper.insert(entity);
        nodeRegistry.setId(entity.getId());
        return nodeRegistry;
    }

    @Override
    public NodeRegistry findById(Long id) {
        return toDomain(nodeRegistryMapper.selectById(id));
    }

    @Override
    public void update(NodeRegistry nodeRegistry) {
        nodeRegistryMapper.updateById(toEntity(nodeRegistry));
    }

    @Override
    public List<NodeRegistry> findByGridName(String gridName) {
        return nodeRegistryMapper.selectList(new LambdaQueryWrapper<NodeRegistryEntity>()
                        .eq(NodeRegistryEntity::getGridName, gridName)
                        .orderByAsc(NodeRegistryEntity::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private NodeRegistry toDomain(NodeRegistryEntity entity) {
        if (entity == null) {
            return null;
        }
        return NodeRegistry.builder()
                .id(entity.getId())
                .gridName(entity.getGridName())
                .ip(entity.getIp())
                .registerTime(entity.getRegisterTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private NodeRegistryEntity toEntity(NodeRegistry nodeRegistry) {
        NodeRegistryEntity entity = new NodeRegistryEntity();
        entity.setId(nodeRegistry.getId());
        entity.setGridName(nodeRegistry.getGridName());
        entity.setIp(nodeRegistry.getIp());
        entity.setRegisterTime(nodeRegistry.getRegisterTime());
        entity.setCreatedAt(nodeRegistry.getCreatedAt());
        entity.setUpdatedAt(nodeRegistry.getUpdatedAt());
        return entity;
    }
}
