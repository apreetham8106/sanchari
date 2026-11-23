package com.sanchari.auth.repository;

import com.sanchari.auth.domain.OtpCode;
import com.sanchari.auth.domain.OtpPurpose;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    Optional<OtpCode> findTopByPhoneAndPurposeAndConsumedFalseOrderByCreatedAtDesc(String phone, OtpPurpose purpose);

    long countByPhoneAndCreatedAtAfter(String phone, Instant since);
}
