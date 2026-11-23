
package com.sanchari.location.repository;

import com.sanchari.location.domain.DriverLocation;
import com.sanchari.location.dto.NearbyDriverView;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DriverLocationRepository
        extends JpaRepository<DriverLocation, UUID> {

    /**
     * PostgreSQL upsert for driver location.
     * Requires driver_id to have a UNIQUE or PRIMARY KEY constraint.
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO driver_locations
                (driver_id, lat, lng, heading, updated_at)
            VALUES
                (:driverId, :lat, :lng, CAST(:heading AS REAL), NOW())
            ON CONFLICT (driver_id) DO UPDATE
            SET lat = EXCLUDED.lat,
                lng = EXCLUDED.lng,
                heading = EXCLUDED.heading,
                updated_at = NOW()
            """)
    void upsert(
            @Param("driverId") UUID driverId,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("heading") Float heading
    );

    /**
     * Find available drivers within the specified radius.
     * Distance is calculated in metres.
     */
    @Query(nativeQuery = true, value = """
            SELECT
                l.driver_id AS "driverId",
                l.lat AS "lat",
                l.lng AS "lng",
                earth_distance(
                    ll_to_earth(:lat, :lng),
                    ll_to_earth(l.lat, l.lng)
                ) AS "distanceM"
            FROM driver_locations l
            JOIN driver_profiles d
                ON d.user_id = l.driver_id
            JOIN users u
                ON u.id = l.driver_id
            WHERE d.is_available = TRUE
              AND d.kyc_status = 'VERIFIED'
              AND u.status = 'ACTIVE'
              AND l.updated_at > :since

              AND EXISTS (
                  SELECT 1
                  FROM vehicles v
                  WHERE v.driver_id = d.user_id
                    AND v.vehicle_type = :type
                    AND v.rc_status = 'VERIFIED'
              )

              AND earth_box(
                    ll_to_earth(:lat, :lng),
                    :radius
                  ) @> ll_to_earth(l.lat, l.lng)

              AND earth_distance(
                    ll_to_earth(:lat, :lng),
                    ll_to_earth(l.lat, l.lng)
                  ) <= :radius

              AND NOT EXISTS (
                  SELECT 1
                  FROM ride_offers o
                  WHERE o.ride_id = :rideId
                    AND o.driver_id = l.driver_id
              )

            ORDER BY "distanceM" ASC
            LIMIT :limit
            """)
    List<NearbyDriverView> findNearbyAvailable(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius,
            @Param("type") String type,
            @Param("rideId") UUID rideId,
            @Param("since") Instant since,
            @Param("limit") int limit
    );    

// ---------------------------------------------------------------------------------------------
    // PostgreSQL (DISABLED) - replace the two MySQL queries above with these to switch back.
    // Requires the cube + earthdistance extensions (see db/postgres/01_schema.postgres.sql).
    // ---------------------------------------------------------------------------------------------
    // @Transactional
    // @Modifying
    // @Query(nativeQuery = true, value = """
    //         INSERT INTO driver_locations (driver_id, lat, lng, heading, updated_at)
    //         VALUES (:driverId, :lat, :lng, CAST(:heading AS REAL), now())
    //         ON CONFLICT (driver_id) DO UPDATE
    //           SET lat = EXCLUDED.lat, lng = EXCLUDED.lng, heading = EXCLUDED.heading, updated_at = now()
    //         """)
    // void upsert(@Param("driverId") UUID driverId, @Param("lat") double lat, @Param("lng") double lng,
    //             @Param("heading") Float heading);
    //
    // @Query(nativeQuery = true, value = """
    //         SELECT l.driver_id AS "driverId", l.lat AS "lat", l.lng AS "lng",
    //                earth_distance(ll_to_earth(:lat, :lng), ll_to_earth(l.lat, l.lng)) AS "distanceM"
    //         FROM driver_locations l
    //         JOIN driver_profiles d ON d.user_id = l.driver_id
    //         JOIN users u ON u.id = l.driver_id
    //         WHERE d.is_available = TRUE
    //           AND d.kyc_status = 'VERIFIED'
    //           AND u.status = 'ACTIVE'
    //           AND l.updated_at > :since
    //           AND EXISTS (SELECT 1 FROM vehicles v
    //                       WHERE v.driver_id = d.user_id AND v.vehicle_type = :type AND v.rc_status = 'VERIFIED')
    //           AND earth_box(ll_to_earth(:lat, :lng), :radius) @> ll_to_earth(l.lat, l.lng)
    //           AND earth_distance(ll_to_earth(:lat, :lng), ll_to_earth(l.lat, l.lng)) <= :radius
    //           AND l.driver_id NOT IN (SELECT o.driver_id FROM ride_offers o WHERE o.ride_id = :rideId)
    //         ORDER BY "distanceM"
    //         LIMIT :limit
    //         """)
    // List<NearbyDriverView> findNearbyAvailable(@Param("lat") double lat, @Param("lng") double lng,
    //                                            @Param("radius") double radius, @Param("type") String type,
    //                                            @Param("rideId") UUID rideId, @Param("since") Instant since,
    //  
}