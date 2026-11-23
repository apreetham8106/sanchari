package com.sanchari.admin.dto;

public record DashboardStatsDto(long totalRiders, long totalDrivers, long onlineDrivers, long pendingKyc,
                                long ridesToday, long activeRides, long completedRides, long cancelledRides) {
}
