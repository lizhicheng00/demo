package com.qq24650393.demo.web;

import com.qq24650393.demo.domain.ListeningService;
import com.qq24650393.demo.web.api.ListeningApi;
import com.qq24650393.demo.web.model.ApiResponseListeningList;
import com.qq24650393.demo.web.model.ApiResponseListeningResponse;
import com.qq24650393.demo.web.model.ChangeListeningStatusRequest;
import com.qq24650393.demo.web.model.CreateListeningRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListeningApiController implements ListeningApi {

    private final ListeningService service;

    public ListeningApiController(ListeningService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<ApiResponseListeningResponse> createListening(CreateListeningRequest request) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseListeningResponse().data(service.create(request))));
    }

    @Override
    public ResponseEntity<ApiResponseListeningList> syncListenings(String nodeCode) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseListeningList().data(service.sync(nodeCode))));
    }

    @Override
    public ResponseEntity<ApiResponseListeningResponse> changeListeningStatus(
            Long id,
            ChangeListeningStatusRequest request) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseListeningResponse().data(service.changeStatus(id, request))));
    }
}
