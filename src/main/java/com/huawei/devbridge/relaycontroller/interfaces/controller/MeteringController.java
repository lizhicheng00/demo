package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.MeteringAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Metering")
@RestController
@RequestMapping("/v1/tunnel")
@RequiredArgsConstructor
public class MeteringController {
    private final MeteringAppService meteringAppService;

    @Operation(summary = "Report tunnel traffic usage")
    @PostMapping("/{gridName}/metering")
    public Result<MeteringReportResponse> report(
            @PathVariable String gridName,
            @Valid @RequestBody MeteringReportRequest request) {
        return Result.success(meteringAppService.report(gridName, request));
    }
}
