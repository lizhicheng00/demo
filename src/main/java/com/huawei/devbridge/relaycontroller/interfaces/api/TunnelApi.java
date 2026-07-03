package com.huawei.devbridge.relaycontroller.interfaces.api;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.interfaces.request.CreateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.request.UpdateTunnelRequest;
import com.huawei.devbridge.relaycontroller.interfaces.response.CreateTunnelResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelDetailResponse;
import com.huawei.devbridge.relaycontroller.interfaces.response.TunnelListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Tunnel")
public interface TunnelApi {
    String USER_HEADER = "X-User-Id";

    @Operation(summary = "List user tunnels")
    @GetMapping("/tunnels")
    Result<List<TunnelListItemResponse>> list(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam(required = false) String gridName);

    @Operation(summary = "Get tunnel detail with access token")
    @GetMapping("/tunnel")
    Result<TunnelDetailResponse> detail(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId);

    @Operation(summary = "Create tunnel")
    @PostMapping("/tunnel")
    Result<CreateTunnelResponse> create(
            @RequestHeader(USER_HEADER) String userId,
            @Valid @RequestBody CreateTunnelRequest request);

    @Operation(summary = "Update tunnel")
    @PutMapping("/tunnel")
    Result<Boolean> update(
            @RequestHeader(USER_HEADER) String userId,
            @Valid @RequestBody UpdateTunnelRequest request);

    @Operation(summary = "Soft delete tunnel")
    @DeleteMapping("/tunnel")
    Result<Boolean> delete(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam String tunnelId);
}
