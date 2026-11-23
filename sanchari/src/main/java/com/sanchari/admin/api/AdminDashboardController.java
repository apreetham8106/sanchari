package com.sanchari.admin.api;

import com.sanchari.admin.dto.DashboardStatsDto;
import com.sanchari.admin.service.AdminService;
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
