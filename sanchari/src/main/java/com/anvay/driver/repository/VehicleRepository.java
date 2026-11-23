package com.anvay.driver.repository;

import com.anvay.driver.domain.RcStatus;
import com.anvay.driver.domain.Vehicle;
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
