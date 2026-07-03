package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.MeteringAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.generated.api.MeteringApi;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeteringController implements MeteringApi {
    private final MeteringAppService meteringAppService;

    @Override
    public Result<MeteringReportResponse> reportMetering(String gridName, MeteringReportRequest request) {
        return Result.success(meteringAppService.report(gridName, request));
    }
}
