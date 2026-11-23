package com.anvay.driver.api;

import com.anvay.common.dto.MessageResponse;
import com.anvay.driver.service.DriverKycService;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dev only: simulates Digio finishing a verification. e.g. POST /api/dev/kyc/STUB-DL-.../approved */
@RestController
@Profile("dev")
@RequestMapping("/api/dev/kyc")
public class DevKycController {

    private final DriverKycService kycService;

    public DevKycController(DriverKycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping("/{requestId}/{status}")
    public MessageResponse finish(@PathVariable String requestId, @PathVariable String status) {
        kycService.applyResult(requestId, status, Map.of("source", "dev-endpoint"));
        return new MessageResponse("Applied " + status + " to " + requestId);
    }
}
