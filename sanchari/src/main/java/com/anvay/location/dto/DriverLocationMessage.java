package com.anvay.location.dto;

import java.time.Instant;
import java.util.UUID;

/** Pushed to /topic/rides/{rideId}/driver-location while a driver is on a ride. */
public record DriverLocationMessage(UUID rideId, UUID driverId, double lat, double lng, Float heading, Instant ts) {
}
