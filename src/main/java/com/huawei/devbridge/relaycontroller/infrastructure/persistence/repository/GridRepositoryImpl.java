package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.converter.PersistenceConverter;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.GridEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper.GridMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GridRepositoryImpl implements GridRepository {
    private final GridMapper gridMapper;
    private final PersistenceConverter converter;

    @Override
    public Grid findByGridNameAndRegion(String gridName, String region) {
        GridEntity entity = gridMapper.selectOne(new LambdaQueryWrapper<GridEntity>()
                .eq(GridEntity::getGrid, gridName)
                .eq(GridEntity::getRegion, region)
                .last("LIMIT 1"));
        return converter.toDomain(entity);
    }
}
