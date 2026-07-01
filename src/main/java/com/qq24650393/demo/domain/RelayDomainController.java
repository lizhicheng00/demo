package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relay-domains")
public class RelayDomainController {

    private final RelayDomainService service;

    public RelayDomainController(RelayDomainService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<RelayDomainResponse> create(@Valid @RequestBody CreateRelayDomainRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping
    public ApiResponse<List<RelayDomainResponse>> list() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<RelayDomainResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<RelayDomainResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRelayDomainRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<RelayDomainResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRelayDomainStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
