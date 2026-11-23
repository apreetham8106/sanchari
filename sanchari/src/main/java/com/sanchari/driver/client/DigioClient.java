package com.sanchari.driver.client;

import java.time.LocalDate;
import java.util.UUID;

/** Starts a Digio verification and returns Digio's request id. The result arrives later via webhook. */
public interface DigioClient {
    String startDriverVerification(UUID driverId, String licenseNumber, LocalDate dateOfBirth);

    String startVehicleVerification(UUID driverId, String registrationNo);
}
