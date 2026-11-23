package com.anvay.location.api;

import com.anvay.common.enums.Role;
import com.anvay.location.dto.LocationUpdateRequest;
import com.anvay.location.service.LocationService;
import com.anvay.security.AnvayUserPrincipal;
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
                && t.getPrincipal() instanceof AnvayUserPrincipal p
                && p.role() == Role.DRIVER
                && req.lat() != null && req.lng() != null) {
            locationService.report(p.userId(), req);
        }
    }
}
