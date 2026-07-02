package com.huawei.devbridge.relaycontroller.application.assembler;

import com.huawei.devbridge.relaycontroller.interfaces.response.MeteringReportResponse;
import org.springframework.stereotype.Component;

@Component
public class MeteringAssembler {

    public MeteringReportResponse accepted() {
        return MeteringReportResponse.builder().accepted(true).build();
    }
}
