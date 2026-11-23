package com.anvay.ride.service;

import static com.anvay.ride.domain.RideStatus.ACCEPTED;
import static com.anvay.ride.domain.RideStatus.ARRIVED;
import static com.anvay.ride.domain.RideStatus.CANCELLED;
import static com.anvay.ride.domain.RideStatus.COMPLETED;
import static com.anvay.ride.domain.RideStatus.NO_DRIVER;
import static com.anvay.ride.domain.RideStatus.SEARCHING;
import static com.anvay.ride.domain.RideStatus.STARTED;

import com.anvay.common.exception.ConflictException;
import com.anvay.ride.domain.RideStatus;
import java.util.Map;
import java.util.Set;

/**
 * SEARCHING -> ACCEPTED -> ARRIVED -> STARTED -> COMPLETED
 * SEARCHING -> NO_DRIVER; SEARCHING | ACCEPTED | ARRIVED -> CANCELLED
 */
public final class RideStateMachine {

    private static final Map<RideStatus, Set<RideStatus>> ALLOWED = Map.of(
            SEARCHING, Set.of(ACCEPTED, CANCELLED, NO_DRIVER),
            ACCEPTED, Set.of(ARRIVED, CANCELLED),
            ARRIVED, Set.of(STARTED, CANCELLED),
            STARTED, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of(),
            NO_DRIVER, Set.of());

    private RideStateMachine() {
    }

    public static boolean canTransition(RideStatus from, RideStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public static void assertTransition(RideStatus from, RideStatus to) {
        if (!canTransition(from, to)) {
            throw new ConflictException("Cannot change ride from " + from + " to " + to);
        }
    }
}
