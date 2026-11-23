package com.anvay.ride.api;

import com.anvay.common.dto.MessageResponse;
import com.anvay.common.dto.PageResponse;
import com.anvay.dispatch.service.OfferService;
import com.anvay.ride.domain.RideStatus;
import com.anvay.ride.dto.RideDto;
import com.anvay.ride.dto.RideOfferDto;
import com.anvay.ride.service.RideService;
import com.anvay.security.AnvayUserPrincipal;
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
    public List<RideOfferDto> offers(@AuthenticationPrincipal AnvayUserPrincipal p) {
        return offerService.listOpenOffers(p.userId());
    }

    @GetMapping("/rides/active")
    public ResponseEntity<RideDto> active(@AuthenticationPrincipal AnvayUserPrincipal p) {
        return rideService.activeForDriver(p.userId()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/rides")
    public PageResponse<RideDto> history(@AuthenticationPrincipal AnvayUserPrincipal p,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return rideService.driverHistory(p.userId(), PageRequest.of(page, Math.min(size, 100)));
    }

    @PostMapping("/rides/{id}/accept")
    public RideDto accept(@AuthenticationPrincipal AnvayUserPrincipal p, @PathVariable UUID id) {
        return rideService.accept(id, p.userId());
    }

    @PostMapping("/rides/{id}/reject")
    public MessageResponse reject(@AuthenticationPrincipal AnvayUserPrincipal p, @PathVariable UUID id) {
        rideService.reject(id, p.userId());
        return new MessageResponse("Rejected");
    }

    @PostMapping("/rides/{id}/arrived")
    public RideDto arrived(@AuthenticationPrincipal AnvayUserPrincipal p, @PathVariable UUID id) {
        return rideService.driverTransition(id, p.userId(), RideStatus.ARRIVED);
    }

    @PostMapping("/rides/{id}/start")
    public RideDto start(@AuthenticationPrincipal AnvayUserPrincipal p, @PathVariable UUID id) {
        return rideService.driverTransition(id, p.userId(), RideStatus.STARTED);
    }

    @PostMapping("/rides/{id}/complete")
    public RideDto complete(@AuthenticationPrincipal AnvayUserPrincipal p, @PathVariable UUID id) {
        return rideService.driverTransition(id, p.userId(), RideStatus.COMPLETED);
    }
}
