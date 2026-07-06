package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Grid;

public interface GridRepository {
    Grid findByGridName(String gridName);

    boolean existsByGridName(String gridName);
}
