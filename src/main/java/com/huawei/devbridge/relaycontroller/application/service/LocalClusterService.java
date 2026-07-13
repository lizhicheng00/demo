package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.Cluster;
import com.huawei.devbridge.relaycontroller.domain.repository.ClusterRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalClusterService {
    private final ClusterRepository clusterRepository;
    private final RelayProperties relayProperties;

    public Cluster requireLocalCluster(String clusterId) {
        Cluster cluster = clusterRepository.findByClusterIdAndRegion(clusterId, relayProperties.getRegion());
        if (cluster == null) {
            throw new BizException(ErrorCode.CLUSTER_NOT_FOUND);
        }
        return cluster;
    }
}
