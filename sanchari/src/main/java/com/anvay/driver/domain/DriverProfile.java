package com.anvay.driver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "driver_profiles")
@Getter
@Setter
@NoArgsConstructor
public class DriverProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private KycStatus kycStatus = KycStatus.NOT_STARTED;

    @Column(name = "digio_request_id")
    private String digioRequestId;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;

    @Column(name = "is_online", nullable = false)
    private boolean online;

    /** Online and not currently on a ride. */
    @Column(name = "is_available", nullable = false)
    private boolean available;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DriverProfile(UUID userId) {
        this.userId = userId;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
