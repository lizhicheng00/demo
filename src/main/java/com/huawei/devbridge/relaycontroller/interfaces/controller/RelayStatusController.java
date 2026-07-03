package com.huawei.devbridge.relaycontroller.interfaces.controller;

import com.huawei.devbridge.relaycontroller.application.service.RelayStatusAppService;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.api.RelayStatusApi;
import com.huawei.devbridge.relaycontroller.interfaces.response.RelayStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RelayStatusController implements RelayStatusApi {
    private final RelayStatusAppService relayStatusAppService;

    @Override
    public Result<RelayStatusResponse> status(String userId, String tunnelId) {
        return Result.success(relayStatusAppService.getStatus(userId, tunnelId));
    }
}
