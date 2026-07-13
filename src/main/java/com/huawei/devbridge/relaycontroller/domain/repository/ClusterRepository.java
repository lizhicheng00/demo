package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Cluster;

public interface ClusterRepository {
    Cluster findByClusterIdAndRegion(String clusterId, String region);
}
