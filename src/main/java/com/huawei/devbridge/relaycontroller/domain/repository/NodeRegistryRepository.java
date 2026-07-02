package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.NodeRegistry;
import java.util.List;

public interface NodeRegistryRepository {
    NodeRegistry save(NodeRegistry nodeRegistry);

    NodeRegistry findById(Long id);

    void update(NodeRegistry nodeRegistry);

    List<NodeRegistry> findByGridName(String gridname);
}
