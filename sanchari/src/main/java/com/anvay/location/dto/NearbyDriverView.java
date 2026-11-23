package com.anvay.location.dto;

import java.util.UUID;

/** Projection of the nearby-driver native query. */
public interface NearbyDriverView {
    UUID getDriverId();

    Double getLat();

    Double getLng();

    Double getDistanceM();
}
