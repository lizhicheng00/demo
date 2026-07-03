package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import java.util.List;

public interface GridRepository {
    Grid findByGridName(String gridName);

    boolean existsByGridName(String gridName);

    List<Grid> findByRegion(String region);
}
