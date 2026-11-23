package com.sanchari.ride.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ride_offers")
@Getter
@Setter
@NoArgsConstructor
public class RideOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ride_id", nullable = false)
    private UUID rideId;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "radius_m", nullable = false)
    private int radiusM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.SENT;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    public RideOffer(UUID rideId, UUID driverId, int radiusM) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.radiusM = radiusM;
    }

    @PrePersist
    void onCreate() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }
}
