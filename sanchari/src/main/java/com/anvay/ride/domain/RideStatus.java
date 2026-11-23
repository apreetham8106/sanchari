package com.anvay.ride.domain;

import java.util.List;

public enum RideStatus {
    SEARCHING, ACCEPTED, ARRIVED, STARTED, COMPLETED, CANCELLED, NO_DRIVER;

    /** Rides a rider cannot start a second one beside. */
    public static final List<RideStatus> ACTIVE = List.of(SEARCHING, ACCEPTED, ARRIVED, STARTED);

    /** Rides during which a driver is occupied. */
    public static final List<RideStatus> DRIVER_BUSY = List.of(ACCEPTED, ARRIVED, STARTED);
}
