package com.sanchari.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sanchari.driver.domain.KycStatus;
import com.sanchari.driver.service.DriverKycService;
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
