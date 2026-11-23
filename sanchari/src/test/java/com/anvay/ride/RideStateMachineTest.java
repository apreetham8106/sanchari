package com.anvay.ride;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anvay.common.exception.ConflictException;
import com.anvay.ride.domain.RideStatus;
import com.anvay.ride.service.RideStateMachine;
import org.junit.jupiter.api.Test;

class RideStateMachineTest {

    @Test
    void happyPathIsAllowed() {
        assertTrue(RideStateMachine.canTransition(RideStatus.SEARCHING, RideStatus.ACCEPTED));
        assertTrue(RideStateMachine.canTransition(RideStatus.ACCEPTED, RideStatus.ARRIVED));
        assertTrue(RideStateMachine.canTransition(RideStatus.ARRIVED, RideStatus.STARTED));
        assertTrue(RideStateMachine.canTransition(RideStatus.STARTED, RideStatus.COMPLETED));
    }

    @Test
    void cancelAllowedBeforeStartOnly() {
        assertTrue(RideStateMachine.canTransition(RideStatus.SEARCHING, RideStatus.CANCELLED));
        assertTrue(RideStateMachine.canTransition(RideStatus.ACCEPTED, RideStatus.CANCELLED));
        assertTrue(RideStateMachine.canTransition(RideStatus.ARRIVED, RideStatus.CANCELLED));
        assertFalse(RideStateMachine.canTransition(RideStatus.STARTED, RideStatus.CANCELLED));
    }

    @Test
    void terminalStatesCannotMove() {
        for (RideStatus terminal : new RideStatus[] {RideStatus.COMPLETED, RideStatus.CANCELLED, RideStatus.NO_DRIVER}) {
            for (RideStatus next : RideStatus.values()) {
                assertFalse(RideStateMachine.canTransition(terminal, next));
            }
        }
    }

    @Test
    void skippingStepsIsRejected() {
        assertThrows(ConflictException.class, () -> RideStateMachine.assertTransition(RideStatus.SEARCHING, RideStatus.STARTED));
        assertDoesNotThrow(() -> RideStateMachine.assertTransition(RideStatus.SEARCHING, RideStatus.NO_DRIVER));
    }
}
