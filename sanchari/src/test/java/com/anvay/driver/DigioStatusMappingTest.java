package com.anvay.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anvay.driver.domain.KycStatus;
import com.anvay.driver.service.DriverKycService;
import org.junit.jupiter.api.Test;

class DigioStatusMappingTest {

    @Test
    void mapsProviderStatuses() {
        assertEquals(KycStatus.VERIFIED, DriverKycService.mapStatus("approved"));
        assertEquals(KycStatus.VERIFIED, DriverKycService.mapStatus("SUCCESS"));
        assertEquals(KycStatus.REJECTED, DriverKycService.mapStatus("rejected"));
        assertEquals(KycStatus.REJECTED, DriverKycService.mapStatus("failed"));
        assertEquals(KycStatus.IN_PROGRESS, DriverKycService.mapStatus("requested"));
        assertEquals(KycStatus.IN_PROGRESS, DriverKycService.mapStatus(null));
    }
}
