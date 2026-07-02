package com.huawei.devbridge.relaycontroller.domain.repository;

import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import java.util.List;

public interface GridRepository {
    Grid findByGridName(String gridname);

    boolean existsByGridName(String gridname);

    List<Grid> findByRegion(String region);
}
