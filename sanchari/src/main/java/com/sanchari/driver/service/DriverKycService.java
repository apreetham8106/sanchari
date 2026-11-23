package com.sanchari.driver.service;

import com.sanchari.common.exception.ConflictException;
import com.sanchari.common.exception.NotFoundException;
import com.sanchari.driver.client.DigioClient;
import com.sanchari.driver.domain.DriverProfile;
import com.sanchari.driver.domain.KycEvent;
import com.sanchari.driver.domain.KycStatus;
import com.sanchari.driver.domain.KycSubject;
import com.sanchari.driver.domain.RcStatus;
import com.sanchari.driver.domain.Vehicle;
import com.sanchari.driver.dto.StartKycRequest;
import com.sanchari.driver.kafka.DriverKycEventProducer;
import com.sanchari.driver.repository.DriverProfileRepository;
import com.sanchari.driver.repository.KycEventRepository;
import com.sanchari.driver.repository.VehicleRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverKycService {

    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;
    private final KycEventRepository kycEvents;
    private final DigioClient digio;
    private final DriverKycEventProducer producer;
    private final DriverAvailabilityService availability;

    public DriverKycService(DriverProfileRepository profiles, VehicleRepository vehicles, KycEventRepository kycEvents,
                            DigioClient digio, DriverKycEventProducer producer, DriverAvailabilityService availability) {
        this.profiles = profiles;
        this.vehicles = vehicles;
        this.kycEvents = kycEvents;
        this.digio = digio;
        this.producer = producer;
        this.availability = availability;
    }

    @Transactional
    public DriverProfile startDriverKyc(UUID driverId, StartKycRequest req) {
        DriverProfile p = profiles.findById(driverId).orElseThrow(() -> new NotFoundException("Driver profile not found"));
        if (p.getKycStatus() == KycStatus.VERIFIED) {
            throw new ConflictException("KYC is already verified");
        }
        if (p.getKycStatus() == KycStatus.IN_PROGRESS) {
            throw new ConflictException("KYC is already in progress");
        }
        String requestId = digio.startDriverVerification(driverId, req.licenseNumber(), req.dateOfBirth());
        p.setLicenseNumber(req.licenseNumber().trim().toUpperCase());
        p.setLicenseExpiry(req.licenseExpiry());
        p.setDigioRequestId(requestId);
        p.setKycStatus(KycStatus.IN_PROGRESS);
        record(driverId, KycSubject.DRIVER, KycStatus.IN_PROGRESS.name(), Map.of("requestId", requestId, "source", "submit"));
        producer.publish(driverId, KycSubject.DRIVER.name(), KycStatus.IN_PROGRESS.name(), requestId);
        return p;
    }

    @Transactional
    public void startVehicleKyc(Vehicle v) {
        String requestId = digio.startVehicleVerification(v.getDriverId(), v.getRegistrationNo());
        v.setDigioRequestId(requestId);
        v.setRcStatus(RcStatus.PENDING);
        record(v.getDriverId(), KycSubject.VEHICLE, RcStatus.PENDING.name(),
                Map.of("requestId", requestId, "vehicleId", v.getId().toString(), "source", "submit"));
        producer.publish(v.getDriverId(), KycSubject.VEHICLE.name(), RcStatus.PENDING.name(), v.getId().toString());
    }

    /** Called by the Digio webhook (or the dev endpoint) when a verification finishes. */
    @Transactional
    public void applyResult(String requestId, String rawStatus, Map<String, Object> raw) {
        KycStatus mapped = mapStatus(rawStatus);
        Optional<DriverProfile> driver = profiles.findByDigioRequestId(requestId);
        if (driver.isPresent()) {
            setDriverStatus(driver.get(), mapped, raw);
            return;
        }
        Vehicle v = vehicles.findByDigioRequestId(requestId)
                .orElseThrow(() -> new NotFoundException("Unknown verification request"));
        setVehicleStatus(v, mapped, raw);
    }

    @Transactional
    public void reviewDriver(UUID driverId, boolean approve, String remarks) {
        DriverProfile p = profiles.findById(driverId).orElseThrow(() -> new NotFoundException("Driver profile not found"));
        setDriverStatus(p, approve ? KycStatus.VERIFIED : KycStatus.REJECTED,
                Map.of("source", "admin", "remarks", remarks == null ? "" : remarks));
    }

    @Transactional
    public void reviewVehicle(UUID vehicleId, boolean approve, String remarks) {
        Vehicle v = vehicles.findById(vehicleId).orElseThrow(() -> new NotFoundException("Vehicle not found"));
        setVehicleStatus(v, approve ? KycStatus.VERIFIED : KycStatus.REJECTED,
                Map.of("source", "admin", "remarks", remarks == null ? "" : remarks));
    }

    private void setDriverStatus(DriverProfile p, KycStatus status, Map<String, Object> raw) {
        p.setKycStatus(status);
        record(p.getUserId(), KycSubject.DRIVER, status.name(), raw);
        producer.publish(p.getUserId(), KycSubject.DRIVER.name(), status.name(), p.getDigioRequestId());
        if (status == KycStatus.REJECTED) {
            availability.forceOffline(p.getUserId());
        }
    }

    private void setVehicleStatus(Vehicle v, KycStatus status, Map<String, Object> raw) {
        RcStatus rc = switch (status) {
            case VERIFIED -> RcStatus.VERIFIED;
            case REJECTED -> RcStatus.REJECTED;
            default -> RcStatus.PENDING;
        };
        v.setRcStatus(rc);
        record(v.getDriverId(), KycSubject.VEHICLE, rc.name(), raw);
        producer.publish(v.getDriverId(), KycSubject.VEHICLE.name(), rc.name(), v.getId().toString());
    }

    private void record(UUID userId, KycSubject subject, String status, Map<String, Object> raw) {
        KycEvent e = new KycEvent();
        e.setUserId(userId);
        e.setSubject(subject);
        e.setStatus(status);
        e.setRawPayload(raw);
        kycEvents.save(e);
    }

    public static KycStatus mapStatus(String raw) {
        String s = raw == null ? "" : raw.toLowerCase(Locale.ROOT);
        if (s.contains("approv") || s.contains("success") || s.contains("complet") || s.contains("verified")) {
            return KycStatus.VERIFIED;
        }
        if (s.contains("reject") || s.contains("fail") || s.contains("declin") || s.contains("expire")) {
            return KycStatus.REJECTED;
        }
        return KycStatus.IN_PROGRESS;
    }
}
