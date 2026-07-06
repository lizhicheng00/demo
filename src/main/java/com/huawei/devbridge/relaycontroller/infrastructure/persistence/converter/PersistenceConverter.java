package com.huawei.devbridge.relaycontroller.infrastructure.persistence.converter;

import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.model.Metering;
import com.huawei.devbridge.relaycontroller.domain.model.Tunnel;
import com.huawei.devbridge.relaycontroller.domain.model.TunnelPort;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.GridEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.MeteringEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.TunnelEntity;
import com.huawei.devbridge.relaycontroller.infrastructure.persistence.entity.TunnelPortEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersistenceConverter {
    Grid toDomain(GridEntity entity);

    Tunnel toDomain(TunnelEntity entity);

    TunnelEntity toEntity(Tunnel tunnel);

    TunnelPort toDomain(TunnelPortEntity entity);

    TunnelPortEntity toEntity(TunnelPort tunnelPort);

    MeteringEntity toEntity(Metering metering);
}
