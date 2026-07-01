package com.qq24650393.demo.web;

import com.qq24650393.demo.domain.RelayDomainService;
import com.qq24650393.demo.web.api.RelayDomainApi;
import com.qq24650393.demo.web.model.ApiResponseRelayDomainList;
import com.qq24650393.demo.web.model.ApiResponseRelayDomainResponse;
import com.qq24650393.demo.web.model.ApiResponseVoid;
import com.qq24650393.demo.web.model.ChangeRelayDomainStatusRequest;
import com.qq24650393.demo.web.model.CreateRelayDomainRequest;
import com.qq24650393.demo.web.model.UpdateRelayDomainRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RelayDomainApiController implements RelayDomainApi {

    private final RelayDomainService service;

    public RelayDomainApiController(RelayDomainService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<ApiResponseRelayDomainResponse> createRelayDomain(CreateRelayDomainRequest request) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseRelayDomainResponse().data(service.create(request))));
    }

    @Override
    public ResponseEntity<ApiResponseRelayDomainList> listRelayDomains() {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseRelayDomainList().data(service.list())));
    }

    @Override
    public ResponseEntity<ApiResponseRelayDomainResponse> getRelayDomain(Long id) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseRelayDomainResponse().data(service.get(id))));
    }

    @Override
    public ResponseEntity<ApiResponseRelayDomainResponse> updateRelayDomain(Long id, UpdateRelayDomainRequest request) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseRelayDomainResponse().data(service.update(id, request))));
    }

    @Override
    public ResponseEntity<ApiResponseRelayDomainResponse> changeRelayDomainStatus(
            Long id,
            ChangeRelayDomainStatusRequest request) {
        return ResponseEntity.ok(WebResponses.success(new ApiResponseRelayDomainResponse().data(service.changeStatus(id, request))));
    }

    @Override
    public ResponseEntity<ApiResponseVoid> deleteRelayDomain(Long id) {
        service.delete(id);
        return ResponseEntity.ok(WebResponses.success(new ApiResponseVoid()));
    }
}
