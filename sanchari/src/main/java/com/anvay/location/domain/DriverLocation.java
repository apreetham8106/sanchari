package com.anvay.location.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Latest known position of a driver. History is not stored here; it lives in the Kafka topic. */
@Entity
@Table(name = "driver_locations")
@Getter
@Setter
@NoArgsConstructor
public class DriverLocation {

    @Id
    @Column(name = "driver_id")
    private UUID driverId;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    private Float heading;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
