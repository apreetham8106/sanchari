package com.sanchari.ride.repository;

import com.sanchari.ride.domain.OfferStatus;
import com.sanchari.ride.domain.RideOffer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RideOfferRepository extends JpaRepository<RideOffer, UUID> {

    List<RideOffer> findByRideIdAndStatus(UUID rideId, OfferStatus status);

    List<RideOffer> findByDriverIdAndStatus(UUID driverId, OfferStatus status);

    Optional<RideOffer> findByRideIdAndDriverId(UUID rideId, UUID driverId);

    long countByRideIdAndStatus(UUID rideId, OfferStatus status);

    @Modifying(flushAutomatically = true)
    @Query("update RideOffer o set o.status = com.sanchari.ride.domain.OfferStatus.EXPIRED "
            + "where o.rideId = :rideId and o.status = com.sanchari.ride.domain.OfferStatus.SENT and o.sentAt < :cutoff")
    int expireStale(@Param("rideId") UUID rideId, @Param("cutoff") Instant cutoff);
}
