package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.GridEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper.GridMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GridRepositoryImpl implements GridRepository {
    private final GridMapper gridMapper;

    @Override
    public Grid findByGridNameAndRegion(String gridName, String region) {
        GridEntity entity = gridMapper.selectOne(new LambdaQueryWrapper<GridEntity>()
                .eq(GridEntity::getGrid, gridName)
                .eq(GridEntity::getRegion, region)
                .last("LIMIT 1"));
        return toDomain(entity);
    }

    private Grid toDomain(GridEntity entity) {
        if (entity == null) {
            return null;
        }
        return Grid.builder()
                .id(entity.getId())
                .grid(entity.getGrid())
                .region(entity.getRegion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
