package com.sanchari.location.service;

import com.sanchari.dispatch.config.DispatchProperties;
import com.sanchari.location.domain.DriverLocation;
import com.sanchari.location.dto.LocationUpdateRequest;
import com.sanchari.location.dto.NearbyDriverView;
import com.sanchari.location.kafka.DriverLocationProducer;
import com.sanchari.location.repository.DriverLocationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private final DriverLocationProducer producer;
    private final DriverLocationRepository repo;
    private final DispatchProperties dispatchProps;

    public LocationService(DriverLocationProducer producer, DriverLocationRepository repo, DispatchProperties dispatchProps) {
        this.producer = producer;
        this.repo = repo;
        this.dispatchProps = dispatchProps;
    }

    /** Accepts a ping: it goes to Kafka, and DriverLocationConsumer stores the latest point. */
    public void report(UUID driverId, LocationUpdateRequest req) {
        producer.publish(driverId, req.lat(), req.lng(), req.heading());
    }

    @Transactional(readOnly = true)
    public List<NearbyDriverView> findNearbyAvailable(double lat, double lng, int radiusM, String vehicleType,
                                                      UUID rideId, int limit) {
        int safeLimit = Math.max(1, limit);
        Instant since = Instant.now().minusSeconds(dispatchProps.locationFreshnessSeconds());
        return repo.findNearbyAvailable(lat, lng, radiusM, vehicleType, rideId, since, PageRequest.of(0, safeLimit));
    }

    @Transactional(readOnly = true)
    public Optional<DriverLocation> find(UUID driverId) {
        return repo.findById(driverId);
    }
}