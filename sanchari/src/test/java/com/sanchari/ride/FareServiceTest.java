package com.sanchari.ride;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchari.common.enums.VehicleType;
import com.sanchari.ride.service.FareService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FareServiceTest {

    private final FareService fares = new FareService();

    @Test
    void minimumFareApplies() {
        assertEquals(new BigDecimal("30.00"), fares.estimate(VehicleType.BIKE, 300));
    }

    @Test
    void longerTripsCostMore() {
        BigDecimal shortTrip = fares.estimate(VehicleType.CAB, 3_000);
        BigDecimal longTrip = fares.estimate(VehicleType.CAB, 12_000);
        assertTrue(longTrip.compareTo(shortTrip) > 0);
    }

    @Test
    void biggerVehiclesCostMore() {
        assertTrue(fares.estimate(VehicleType.SUV, 8_000).compareTo(fares.estimate(VehicleType.BIKE, 8_000)) > 0);
    }
}
