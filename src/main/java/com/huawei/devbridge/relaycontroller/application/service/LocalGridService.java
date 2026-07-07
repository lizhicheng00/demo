package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.model.Grid;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalGridService {
    private final GridRepository gridRepository;
    private final RelayProperties relayProperties;

    public Grid requireLocalGrid(String gridName) {
        Grid grid = gridRepository.findByGridNameAndRegion(gridName, relayProperties.getRegion());
        if (grid == null) {
            throw new BizException(ErrorCode.GRID_NOT_FOUND);
        }
        return grid;
    }
}
