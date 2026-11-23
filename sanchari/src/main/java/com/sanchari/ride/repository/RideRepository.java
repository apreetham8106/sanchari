package com.sanchari.ride.repository;

import com.sanchari.ride.domain.Ride;
import com.sanchari.ride.domain.RideStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RideRepository extends JpaRepository<Ride, UUID> {

    /** Row lock: serialises accept, cancel, status changes and dispatch rounds for one ride. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Ride r where r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByRiderIdAndStatusIn(UUID riderId, Collection<RideStatus> statuses);

    boolean existsByDriverIdAndStatusIn(UUID driverId, Collection<RideStatus> statuses);

    Optional<Ride> findFirstByRiderIdAndStatusInOrderByCreatedAtDesc(UUID riderId, Collection<RideStatus> statuses);

    Optional<Ride> findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(UUID driverId, Collection<RideStatus> statuses);

    Page<Ride> findByRiderIdOrderByCreatedAtDesc(UUID riderId, Pageable pageable);

    Page<Ride> findByDriverIdOrderByCreatedAtDesc(UUID driverId, Pageable pageable);

    Page<Ride> findByStatus(RideStatus status, Pageable pageable);

    @Query("select r.id from Ride r where r.status = :status")
    List<UUID> findIdsByStatus(@Param("status") RideStatus status);

    long countByStatus(RideStatus status);

    long countByStatusIn(Collection<RideStatus> statuses);

    long countByCreatedAtAfter(Instant since);
}
