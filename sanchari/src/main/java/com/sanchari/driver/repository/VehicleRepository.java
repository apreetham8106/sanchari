package com.sanchari.driver.repository;

import com.sanchari.driver.domain.RcStatus;
import com.sanchari.driver.domain.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByDriverId(UUID driverId);

    Optional<Vehicle> findByDigioRequestId(String digioRequestId);

    boolean existsByDriverIdAndRcStatus(UUID driverId, RcStatus status);

    boolean existsByRegistrationNo(String registrationNo);
}
