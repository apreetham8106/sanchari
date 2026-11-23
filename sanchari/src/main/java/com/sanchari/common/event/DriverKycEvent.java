package com.sanchari.common.event;

import java.util.UUID;

public record DriverKycEvent(UUID driverId, String subject, String status, String refId) {
}
