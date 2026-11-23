package com.anvay.driver.repository;

import com.anvay.driver.domain.DriverProfile;
import com.anvay.driver.domain.KycStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {
    Optional<DriverProfile> findByDigioRequestId(String digioRequestId);

    Page<DriverProfile> findByKycStatus(KycStatus status, Pageable pageable);

    long countByOnlineTrue();

    long countByKycStatus(KycStatus status);
}
