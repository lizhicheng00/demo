package com.huawei.devbridge.relaycontroller.infrastructure.persistence.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Metering;
import com.huawei.devbridge.relaycontroller.domain.repository.MeteringRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.converter.PersistenceConverter;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.mapper.MeteringMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MeteringRepositoryImpl implements MeteringRepository {
    private final MeteringMapper meteringMapper;
    private final PersistenceConverter converter;

    @Override
    public void save(Metering metering) {
        meteringMapper.insert(converter.toEntity(metering));
    }
}
