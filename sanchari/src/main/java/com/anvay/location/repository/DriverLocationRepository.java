package com.anvay.location.repository;

import com.anvay.location.domain.DriverLocation;
import com.anvay.location.dto.NearbyDriverView;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, UUID> {

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO driver_locations (driver_id, lat, lng, heading, updated_at)
            VALUES (:driverId, :lat, :lng, CAST(:heading AS REAL), now())
            ON CONFLICT (driver_id) DO UPDATE
              SET lat = EXCLUDED.lat, lng = EXCLUDED.lng, heading = EXCLUDED.heading, updated_at = now()
            """)
    void upsert(@Param("driverId") UUID driverId, @Param("lat") double lat, @Param("lng") double lng,
                @Param("heading") Float heading);

    /**
     * Available, KYC-verified, active drivers with a verified vehicle of the requested type, with a recent
     * location inside the radius, nearest first, skipping drivers who were already offered this ride.
     */
    @Query(nativeQuery = true, value = """
            SELECT l.driver_id AS "driverId", l.lat AS "lat", l.lng AS "lng",
                   earth_distance(ll_to_earth(:lat, :lng), ll_to_earth(l.lat, l.lng)) AS "distanceM"
            FROM driver_locations l
            JOIN driver_profiles d ON d.user_id = l.driver_id
            JOIN users u ON u.id = l.driver_id
            WHERE d.is_available = TRUE
              AND d.kyc_status = 'VERIFIED'
              AND u.status = 'ACTIVE'
              AND l.updated_at > :since
              AND EXISTS (SELECT 1 FROM vehicles v
                          WHERE v.driver_id = d.user_id AND v.vehicle_type = :type AND v.rc_status = 'VERIFIED')
              AND earth_box(ll_to_earth(:lat, :lng), :radius) @> ll_to_earth(l.lat, l.lng)
              AND earth_distance(ll_to_earth(:lat, :lng), ll_to_earth(l.lat, l.lng)) <= :radius
              AND l.driver_id NOT IN (SELECT o.driver_id FROM ride_offers o WHERE o.ride_id = :rideId)
            ORDER BY "distanceM"
            LIMIT :limit
            """)
    List<NearbyDriverView> findNearbyAvailable(@Param("lat") double lat, @Param("lng") double lng,
                                               @Param("radius") double radius, @Param("type") String type,
                                               @Param("rideId") UUID rideId, @Param("since") Instant since,
                                               @Param("limit") int limit);
}
