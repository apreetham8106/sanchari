package com.sanchari.ride.api;

import com.sanchari.common.dto.PageResponse;
import com.sanchari.ride.dto.BookRideRequest;
import com.sanchari.ride.dto.FareEstimateDto;
import com.sanchari.ride.dto.FareEstimateRequest;
import com.sanchari.ride.dto.RideDto;
import com.sanchari.ride.service.RideService;
import com.sanchari.security.SanchariUserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Rider-facing ride API (role RIDER). */
@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/estimate")
    public FareEstimateDto estimate(@Valid @RequestBody FareEstimateRequest req) {
        return rideService.estimate(req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RideDto book(@AuthenticationPrincipal SanchariUserPrincipal p, @Valid @RequestBody BookRideRequest req) {
        return rideService.book(p.userId(), req);
    }

    @GetMapping("/active")
    public ResponseEntity<RideDto> active(@AuthenticationPrincipal SanchariUserPrincipal p) {
        return rideService.activeForRider(p.userId()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public PageResponse<RideDto> history(@AuthenticationPrincipal SanchariUserPrincipal p,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return rideService.riderHistory(p.userId(), PageRequest.of(page, Math.min(size, 100)));
    }

    @GetMapping("/{id}")
    public RideDto get(@AuthenticationPrincipal SanchariUserPrincipal p, @PathVariable UUID id) {
        return rideService.get(id, p.userId(), p.role());
    }

    @PostMapping("/{id}/cancel")
    public RideDto cancel(@AuthenticationPrincipal SanchariUserPrincipal p, @PathVariable UUID id) {
        return rideService.cancel(id, p.userId());
    }
}
