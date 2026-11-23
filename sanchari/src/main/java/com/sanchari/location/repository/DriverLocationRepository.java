package com.sanchari.location.repository;

import com.sanchari.location.domain.DriverLocation;
import com.sanchari.location.dto.NearbyDriverView;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DriverLocationRepository
        extends JpaRepository<DriverLocation, UUID> {

    /**
     * MySQL upsert for driver location.
     * Requires driver_id to have a PRIMARY KEY or UNIQUE index.
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO driver_locations
                (driver_id, lat, lng, heading, updated_at)
            VALUES
                (:driverId, :lat, :lng, CAST(:heading AS DECIMAL(6, 2)), CURRENT_TIMESTAMP(6))
            ON DUPLICATE KEY UPDATE
                lat = VALUES(lat),
                lng = VALUES(lng),
                heading = VALUES(heading),
                updated_at = CURRENT_TIMESTAMP(6)
            """)
    void upsert(
            @Param("driverId") UUID driverId,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("heading") Float heading
    );

    /**
     * Find available drivers within the specified radius (in metres) using MySQL.
     */
    @Query(nativeQuery = true, value = """
            SELECT
                sub.driverId AS driverId,
                sub.lat AS lat,
                sub.lng AS lng,
                sub.distanceM AS distanceM
            FROM (
                SELECT
                    l.driver_id AS driverId,
                    l.lat AS lat,
                    l.lng AS lng,
                    (
                        12742000.0 * ASIN(
                            LEAST(1.0, SQRT(
                                POWER(SIN(RADIANS(l.lat - :lat) / 2), 2)
                                + COS(RADIANS(:lat)) * COS(RADIANS(l.lat))
                                * POWER(SIN(RADIANS(l.lng - :lng) / 2), 2)
                            ))
                        )
                    ) AS distanceM
                FROM driver_locations l
                JOIN driver_profiles d
                    ON d.user_id = l.driver_id
                JOIN users u
                    ON u.id = l.driver_id
                WHERE d.is_available = TRUE
                  AND d.kyc_status = 'VERIFIED'
                  AND u.status = 'ACTIVE'
                  AND l.updated_at > :since
                  AND l.lat BETWEEN (:lat - (:radius / 111320.0))
                                AND (:lat + (:radius / 111320.0))
                  AND l.lng BETWEEN (:lng - (:radius / (111320.0 * GREATEST(COS(RADIANS(:lat)), 0.0001))))
                                AND (:lng + (:radius / (111320.0 * GREATEST(COS(RADIANS(:lat)), 0.0001))))
                  AND EXISTS (
                      SELECT 1
                      FROM vehicles v
                      WHERE v.driver_id = d.user_id
                        AND v.vehicle_type = :type
                        AND v.rc_status = 'VERIFIED'
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM ride_offers o
                      WHERE o.ride_id = :rideId
                        AND o.driver_id = l.driver_id
                  )
            ) sub
            WHERE sub.distanceM <= :radius
            ORDER BY sub.distanceM ASC
            """)
    List<NearbyDriverView> findNearbyAvailable(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius,
            @Param("type") String type,
            @Param("rideId") UUID rideId,
            @Param("since") Instant since,
            Pageable pageable
    );
}

//
//package com.sanchari.location.repository;
//
//import com.sanchari.location.domain.DriverLocation;
//import com.sanchari.location.dto.NearbyDriverView;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.transaction.annotation.Transactional;
//
//public interface DriverLocationRepository
//        extends JpaRepository<DriverLocation, UUID> {
//
//    /**
//     * PostgreSQL upsert for driver location.
//     * Requires driver_id to have a UNIQUE or PRIMARY KEY constraint.
//     */
//    @Transactional
//    @Modifying
//    @Query(nativeQuery = true, value = """
//            INSERT INTO driver_locations
//                (driver_id, lat, lng, heading, updated_at)
//            VALUES
//                (:driverId, :lat, :lng, CAST(:heading AS REAL), NOW())
//            ON CONFLICT (driver_id) DO UPDATE
//            SET lat = EXCLUDED.lat,
//                lng = EXCLUDED.lng,
//                heading = EXCLUDED.heading,
//                updated_at = NOW()
//            """)
//    void upsert(
//            @Param("driverId") UUID driverId,
//            @Param("lat") double lat,
//            @Param("lng") double lng,
//            @Param("heading") Float heading
//    );
//
//    /**
//     * Find available drivers within the specified radius.
//     * Distance is calculated in metres.
//     */
//    @Query(nativeQuery = true, value = """
//            SELECT
//                l.driver_id AS "driverId",
//                l.lat AS "lat",
//                l.lng AS "lng",
//                earth_distance(
//                    ll_to_earth(:lat, :lng),
//                    ll_to_earth(l.lat, l.lng)
//                ) AS "distanceM"
//            FROM driver_locations l
//            JOIN driver_profiles d
//                ON d.user_id = l.driver_id
//            JOIN users u
//                ON u.id = l.driver_id
//            WHERE d.is_available = TRUE
//              AND d.kyc_status = 'VERIFIED'
//              AND u.status = 'ACTIVE'
//              AND l.updated_at > :since
//
//              AND EXISTS (
//                  SELECT 1
//                  FROM vehicles v
//                  WHERE v.driver_id = d.user_id
//                    AND v.vehicle_type = :type
//                    AND v.rc_status = 'VERIFIED'
//              )
//
//              AND earth_box(
//                    ll_to_earth(:lat, :lng),
//                    :radius
//                  ) @> ll_to_earth(l.lat, l.lng)
//
//              AND earth_distance(
//                    ll_to_earth(:lat, :lng),
//                    ll_to_earth(l.lat, l.lng)
//                  ) <= :radius
//
//              AND NOT EXISTS (
//                  SELECT 1
//                  FROM ride_offers o
//                  WHERE o.ride_id = :rideId
//                    AND o.driver_id = l.driver_id
//              )
//
//            ORDER BY "distanceM" ASC
//            LIMIT :limit
//            """)
//    List<NearbyDriverView> findNearbyAvailable(
//            @Param("lat") double lat,
//            @Param("lng") double lng,
//            @Param("radius") double radius,
//            @Param("type") String type,
//            @Param("rideId") UUID rideId,
//            @Param("since") Instant since,
//            @Param("limit") int limit
//    );    
//
//// ---------------------------------------------------------------------------------------------
//    // PostgreSQL (DISABLED) - replace the two MySQL queries above with these to switch back.
//    // Requires the cube + earthdistance extensions (see db/postgres/01_schema.postgres.sql).
//    // ---------------------------------------------------------------------------------------------
//    // @Transactional
//    // @Modifying
//    // @Query(nativeQuery = true, value = """
//    //         INSERT INTO driver_locations (driver_id, lat, lng, heading, updated_at)
//    //         VALUES (:driverId, :lat, :lng, CAST(:heading AS REAL), now())
//    //         ON CONFLICT (driver_id) DO UPDATE
//    //           SET lat = EXCLUDED.lat, lng = EXCLUDED.lng, heading = EXCLUDED.heading, updated_at = now()
//    //         """)
//    // void upsert(@Param("driverId") UUID driverId, @Param("lat") double lat, @Param("lng") double lng,
//    //             @Param("heading") Float heading);
//    //
//    // @Query(nativeQuery = true, value = """
//    //         SELECT l.driver_id AS "driverId", l.lat AS "lat", l.lng AS "lng",
//    //                earth_distance(ll_to_earth(:lat, :lng), ll_to_earth(l.lat, l.lng)) AS "distanceM"
//    //         FROM driver_locations l
//    //         JOIN driver_profiles d ON d.user_id = l.driver_id
//    //         JOIN users u ON u.id = l.driver_id
//    //         WHERE d.is_available = TRUE
//    //           AND d.kyc_status = 'VERIFIED'
//    //           AND u.status = 'ACTIVE'
//    //           AND l.updated_at > :since
//    //           AND EXISTS (SELECT 1 FROM vehicles v
//    //                       WHERE v.driver_id = d.user_id AND v.vehicle_type = :type AND v.rc_status = 'VERIFIED')
//    //           AND earth_box(ll_to_earth(:lat, :lng), :radius) @> ll_to_earth(l.lat, l.lng)
//    //           AND earth_distance(ll_to_earth(:lat, :lng), ll_to_earth(l.lat, l.lng)) <= :radius
//    //           AND l.driver_id NOT IN (SELECT o.driver_id FROM ride_offers o WHERE o.ride_id = :rideId)
//    //         ORDER BY "distanceM"
//    //         LIMIT :limit
//    //         """)
//    // List<NearbyDriverView> findNearbyAvailable(@Param("lat") double lat, @Param("lng") double lng,
//    //                                            @Param("radius") double radius, @Param("type") String type,
//    //                                            @Param("rideId") UUID rideId, @Param("since") Instant since,
//    //  
//}