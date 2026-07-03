package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.GridConfigAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.GridConfigApi;
import com.huawei.devbridge.relaycontroller.interfaces.response.GridConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GridConfigController implements GridConfigApi {
    private final GridConfigAppService gridConfigAppService;

    @Override
    public Result<GridConfigResponse> getGridConfig(String gridName) {
        return Result.success(gridConfigAppService.getConfig(gridName));
    }
}
