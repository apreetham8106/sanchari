package com.sanchari.common.event;

import java.time.Instant;
import java.util.UUID;

public record DriverLocationEvent(UUID driverId, double lat, double lng, Float heading, Instant ts) {
}
