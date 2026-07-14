package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huawei.devbridge.relaycontroller.domain.model.Cluster;
import com.huawei.devbridge.relaycontroller.domain.repository.ClusterRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.converter.PersistenceConverter;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.ClusterEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper.ClusterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClusterRepositoryImpl implements ClusterRepository {
    private final ClusterMapper clusterMapper;
    private final PersistenceConverter converter;

    @Override
    public Cluster findByClusterIdAndRegion(String clusterId, String region) {
        ClusterEntity entity = clusterMapper.selectOne(new LambdaQueryWrapper<ClusterEntity>()
                .eq(ClusterEntity::getClusterId, clusterId)
                .eq(ClusterEntity::getRegion, region));
        return converter.toDomain(entity);
    }
}
