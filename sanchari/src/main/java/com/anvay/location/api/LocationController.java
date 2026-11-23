package com.anvay.location.api;

import com.anvay.common.dto.MessageResponse;
import com.anvay.location.dto.LocationUpdateRequest;
import com.anvay.location.service.LocationService;
import com.anvay.security.AnvayUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageResponse update(@AuthenticationPrincipal AnvayUserPrincipal p, @Valid @RequestBody LocationUpdateRequest req) {
        locationService.report(p.userId(), req);
        return new MessageResponse("ok");
    }
}
