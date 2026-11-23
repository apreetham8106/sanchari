package com.anvay.admin.api;

import com.anvay.common.dto.PageResponse;
import com.anvay.ride.domain.RideStatus;
import com.anvay.ride.dto.RideDto;
import com.anvay.ride.service.RideService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rides")
public class AdminRideController {

    private final RideService rideService;

    public AdminRideController(RideService rideService) {
        this.rideService = rideService;
    }

    @GetMapping
    public PageResponse<RideDto> list(@RequestParam(required = false) RideStatus status,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return rideService.adminList(status, PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
