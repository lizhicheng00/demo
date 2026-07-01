package com.qq24650393.demo.ops;

import com.qq24650393.demo.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ops/stats")
public class OpsStatsController {

    private final OpsStatsService service;

    public OpsStatsController(OpsStatsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ApiResponse<OpsOverviewResponse> overview() {
        return ApiResponse.ok(service.overview());
    }

    @GetMapping("/traffic")
    public ApiResponse<List<TrafficPointResponse>> traffic() {
        return ApiResponse.ok(service.traffic());
    }
}
