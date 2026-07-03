package com.huawei.devbridge.relaycontroller.interfaces.api;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.response.GridConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Grid Config")
public interface GridConfigApi {

    @Operation(summary = "Get JWT public keys for a grid")
    @GetMapping("/{gridName}/config")
    Result<GridConfigResponse> config(@PathVariable String gridName);
}
