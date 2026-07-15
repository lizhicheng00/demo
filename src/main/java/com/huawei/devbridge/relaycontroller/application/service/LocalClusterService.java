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
    private static final int MAX_CLUSTER_ID_LENGTH = 128;

    private final ClusterRepository clusterRepository;
    private final RelayProperties relayProperties;

    public Cluster requireLocalCluster(String clusterId) {
        if (clusterId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "clusterId is invalid");
        }
        String normalized = clusterId.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_CLUSTER_ID_LENGTH
                || !normalized.equals(clusterId)
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "clusterId is invalid");
        }
        Cluster cluster = clusterRepository.findByClusterIdAndRegion(normalized, relayProperties.getRegion());
        if (cluster == null) {
            throw new BizException(ErrorCode.CLUSTER_NOT_FOUND);
        }
        return cluster;
    }
}
