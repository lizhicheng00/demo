package com.qq24650393.demo.domain;

import com.qq24650393.demo.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listenings")
public class ListeningController {

    private final ListeningService service;

    public ListeningController(ListeningService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<ListeningResponse> create(@Valid @RequestBody CreateListeningRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping("/sync")
    public ApiResponse<List<ListeningResponse>> sync(@RequestParam String nodeCode) {
        return ApiResponse.ok(service.sync(nodeCode));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ListeningResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeListeningStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request));
    }
}
