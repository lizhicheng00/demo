package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.MeteringAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/tunnel")
@RequiredArgsConstructor
public class MeteringController {
    private final MeteringAppService meteringAppService;

    @PostMapping("/{gridName}/metering")
    public Result<MeteringReportResponse> report(
            @PathVariable String gridName,
            @Valid @RequestBody MeteringReportRequest request) {
        return Result.success(meteringAppService.report(gridName, request));
    }
}
