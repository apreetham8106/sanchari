package com.anvay.admin.api;

import com.anvay.admin.dto.DashboardStatsDto;
import com.anvay.admin.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminService admin;

    public AdminDashboardController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping
    public DashboardStatsDto stats() {
        return admin.dashboard();
    }
}
