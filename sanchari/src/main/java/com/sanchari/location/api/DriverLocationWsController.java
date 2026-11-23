package com.sanchari.location.api;

import com.sanchari.common.enums.Role;
import com.sanchari.location.dto.LocationUpdateRequest;
import com.sanchari.location.service.LocationService;
import com.sanchari.security.SanchariUserPrincipal;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

/** WebSocket alternative to POST /api/driver/location: SEND to /app/driver/location. */
@Controller
public class DriverLocationWsController {

    private final LocationService locationService;

    public DriverLocationWsController(LocationService locationService) {
        this.locationService = locationService;
    }

    @MessageMapping("/driver/location")
    public void onLocation(LocationUpdateRequest req, Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken t
                && t.getPrincipal() instanceof SanchariUserPrincipal p
                && p.role() == Role.DRIVER
                && req.lat() != null && req.lng() != null) {
            locationService.report(p.userId(), req);
        }
    }
}
