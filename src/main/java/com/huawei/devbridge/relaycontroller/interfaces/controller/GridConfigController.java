package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.GridConfigAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.response.GridConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/tunnel")
@RequiredArgsConstructor
public class GridConfigController {
    private final GridConfigAppService gridConfigAppService;

    @GetMapping("/{gridName}/config")
    public Result<GridConfigResponse> config(@PathVariable String gridName) {
        return Result.success(gridConfigAppService.getConfig(gridName));
    }
}
