package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Metering;
import com.huawei.devbridge.relaycontroller.domain.repository.MeteringRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.MeteringEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper.MeteringMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MeteringRepositoryImpl implements MeteringRepository {
    private final MeteringMapper meteringMapper;

    @Override
    public void save(Metering metering) {
        MeteringEntity entity = new MeteringEntity();
        entity.setId(metering.getId());
        entity.setGridName(metering.getGridName());
        entity.setTunnelCode(metering.getTunnelCode());
        entity.setTunnelId(metering.getTunnelId());
        entity.setUsageBytes(metering.getUsageBytes());
        entity.setReportedAt(metering.getReportedAt());
        entity.setCreatedAt(metering.getCreatedAt());
        meteringMapper.insert(entity);
    }
}
