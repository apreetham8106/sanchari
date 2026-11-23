package com.anvay.driver.client;

import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Dev stand-in. Finish a verification with POST /api/dev/kyc/{requestId}/{approved|rejected}. */
@Component
@ConditionalOnProperty(name = "anvay.digio.mode", havingValue = "stub", matchIfMissing = true)
public class StubDigioClient implements DigioClient {

    private static final Logger log = LoggerFactory.getLogger(StubDigioClient.class);

    @Override
    public String startDriverVerification(UUID driverId, String licenseNumber, LocalDate dateOfBirth) {
        String id = "STUB-DL-" + UUID.randomUUID();
        log.info("[Digio stub] driver verification started: requestId={}", id);
        return id;
    }

    @Override
    public String startVehicleVerification(UUID driverId, String registrationNo) {
        String id = "STUB-RC-" + UUID.randomUUID();
        log.info("[Digio stub] vehicle verification started: requestId={}", id);
        return id;
    }
}
