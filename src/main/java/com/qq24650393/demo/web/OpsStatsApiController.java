package com.qq24650393.demo.web;

import com.qq24650393.demo.ops.OpsStatsService;
import com.qq24650393.demo.web.api.OpsStatsApi;
import com.qq24650393.demo.web.model.ApiResponseOpsOverviewResponse;
import com.qq24650393.demo.web.model.ApiResponseTrafficPointList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpsStatsApiController implements OpsStatsApi {

    private final OpsStatsService service;

    public OpsStatsApiController(OpsStatsService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<ApiResponseOpsOverviewResponse> getOpsOverview() {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseOpsOverviewResponse().data(service.overview())));
    }

    @Override
    public ResponseEntity<ApiResponseTrafficPointList> listTrafficStats() {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseTrafficPointList().data(service.traffic())));
    }
}
