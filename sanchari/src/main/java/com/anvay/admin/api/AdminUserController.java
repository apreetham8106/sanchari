package com.anvay.admin.api;

import com.anvay.admin.service.AdminService;
import com.anvay.common.dto.PageResponse;
import com.anvay.common.enums.Role;
import com.anvay.user.dto.UserDto;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminService admin;

    public AdminUserController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping
    public PageResponse<UserDto> list(@RequestParam(required = false) Role role,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return admin.users(role, q, PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping("/{id}/block")
    public UserDto block(@PathVariable UUID id) {
        return admin.setBlocked(id, true);
    }

    @PostMapping("/{id}/unblock")
    public UserDto unblock(@PathVariable UUID id) {
        return admin.setBlocked(id, false);
    }
}
