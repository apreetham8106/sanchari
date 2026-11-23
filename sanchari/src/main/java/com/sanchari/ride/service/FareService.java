package com.sanchari.ride.service;

import com.sanchari.common.enums.VehicleType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/** Simple distance-based fare. Replace with real pricing (surge, time, tolls) when ready. */
@Service
public class FareService {

    /** Straight-line distance understates road distance; this factor approximates it. */
    private static final double ROAD_FACTOR = 1.3;

    private record Rate(double base, double perKm, double minimum) {
    }

    private Rate rate(VehicleType type) {
        return switch (type) {
            case BIKE -> new Rate(20, 7, 30);
            case AUTO -> new Rate(30, 11, 45);
            case CAB -> new Rate(50, 15, 80);
            case SUV -> new Rate(80, 20, 120);
        };
    }

    public double roadKm(double straightLineMeters) {
        return straightLineMeters / 1000.0 * ROAD_FACTOR;
    }

    public BigDecimal estimate(VehicleType type, double straightLineMeters) {
        Rate r = rate(type);
        double fare = Math.max(r.minimum(), r.base() + r.perKm() * roadKm(straightLineMeters));
        return BigDecimal.valueOf(fare).setScale(0, RoundingMode.HALF_UP).setScale(2);
    }
}
