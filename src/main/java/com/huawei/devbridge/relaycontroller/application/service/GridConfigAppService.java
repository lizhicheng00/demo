package com.huawei.devbridge.relaycontroller.application.service;

import com.huawei.devbridge.relaycontroller.common.exception.BizException;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.domain.repository.GridRepository;
import com.huawei.devbridge.relaycontroller.domain.service.JwtTokenService;
import com.huawei.devbridge.relaycontroller.interfaces.response.GridConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GridConfigAppService {
    private final GridRepository gridRepository;
    private final JwtTokenService jwtTokenService;

    public GridConfigResponse getConfig(String gridName) {
        if (!gridRepository.existsByGridName(gridName)) {
            throw new BizException(ErrorCode.GRID_NOT_FOUND);
        }
        return GridConfigResponse.builder()
                .jwtPublicKeys(jwtTokenService.getPublicKeys())
                .build();
    }
}
