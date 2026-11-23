package com.sanchari.ride.api;

import com.sanchari.common.dto.MessageResponse;
import com.sanchari.common.dto.PageResponse;
import com.sanchari.dispatch.service.OfferService;
import com.sanchari.ride.domain.RideStatus;
import com.sanchari.ride.dto.RideDto;
import com.sanchari.ride.dto.RideOfferDto;
import com.sanchari.ride.service.RideService;
import com.sanchari.security.SanchariUserPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Driver-facing ride API (role DRIVER). */
@RestController
@RequestMapping("/api/driver")
public class DriverRideController {

    private final RideService rideService;
    private final OfferService offerService;

    public DriverRideController(RideService rideService, OfferService offerService) {
        this.rideService = rideService;
        this.offerService = offerService;
    }

    @GetMapping("/offers")
    public List<RideOfferDto> offers(@AuthenticationPrincipal SanchariUserPrincipal p) {
        return offerService.listOpenOffers(p.userId());
    }

    @GetMapping("/rides/active")
    public ResponseEntity<RideDto> active(@AuthenticationPrincipal SanchariUserPrincipal p) {
        return rideService.activeForDriver(p.userId()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/rides")
    public PageResponse<RideDto> history(@AuthenticationPrincipal SanchariUserPrincipal p,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return rideService.driverHistory(p.userId(), PageRequest.of(page, Math.min(size, 100)));
    }

    @PostMapping("/rides/{id}/accept")
    public RideDto accept(@AuthenticationPrincipal SanchariUserPrincipal p, @PathVariable UUID id) {
        return rideService.accept(id, p.userId());
    }

    @PostMapping("/rides/{id}/reject")
    public MessageResponse reject(@AuthenticationPrincipal SanchariUserPrincipal p, @PathVariable UUID id) {
        rideService.reject(id, p.userId());
        return new MessageResponse("Rejected");
    }

    @PostMapping("/rides/{id}/arrived")
    public RideDto arrived(@AuthenticationPrincipal SanchariUserPrincipal p, @PathVariable UUID id) {
        return rideService.driverTransition(id, p.userId(), RideStatus.ARRIVED);
    }

    @PostMapping("/rides/{id}/start")
    public RideDto start(@AuthenticationPrincipal SanchariUserPrincipal p, @PathVariable UUID id) {
        return rideService.driverTransition(id, p.userId(), RideStatus.STARTED);
    }

    @PostMapping("/rides/{id}/complete")
    public RideDto complete(@AuthenticationPrincipal SanchariUserPrincipal p, @PathVariable UUID id) {
        return rideService.driverTransition(id, p.userId(), RideStatus.COMPLETED);
    }
}
