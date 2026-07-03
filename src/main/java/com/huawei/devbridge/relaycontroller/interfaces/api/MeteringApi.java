package com.huawei.devbridge.relaycontroller.interfaces.api;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.MeteringReportRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Metering")
public interface MeteringApi {

    @Operation(summary = "Report tunnel traffic usage")
    @PostMapping("/{gridName}/metering")
    Result<MeteringReportResponse> report(
            @PathVariable String gridName,
            @Valid @RequestBody MeteringReportRequest request);
}
