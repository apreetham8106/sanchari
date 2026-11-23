package com.anvay.admin.api;

import com.anvay.admin.dto.AdminDriverDto;
import com.anvay.admin.service.AdminService;
import com.anvay.common.dto.MessageResponse;
import com.anvay.common.dto.PageResponse;
import com.anvay.driver.domain.KycStatus;
import com.anvay.driver.dto.KycReviewRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDriverController {

    private final AdminService admin;

    public AdminDriverController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping("/drivers")
    public PageResponse<AdminDriverDto> drivers(@RequestParam(required = false) KycStatus kycStatus,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return admin.drivers(kycStatus, PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "updatedAt")));
    }

    @PostMapping("/drivers/{id}/kyc-review")
    public MessageResponse reviewDriver(@PathVariable UUID id, @Valid @RequestBody KycReviewRequest req) {
        admin.reviewDriver(id, req.approve(), req.remarks());
        return new MessageResponse(req.approve() ? "Driver approved" : "Driver rejected");
    }

    @PostMapping("/vehicles/{id}/review")
    public MessageResponse reviewVehicle(@PathVariable UUID id, @Valid @RequestBody KycReviewRequest req) {
        admin.reviewVehicle(id, req.approve(), req.remarks());
        return new MessageResponse(req.approve() ? "Vehicle approved" : "Vehicle rejected");
    }
}
