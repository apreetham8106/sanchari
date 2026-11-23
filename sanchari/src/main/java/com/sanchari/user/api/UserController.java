package com.sanchari.user.api;

import com.sanchari.security.SanchariUserPrincipal;
import com.sanchari.user.dto.UpdateProfileRequest;
import com.sanchari.user.dto.UserDto;
import com.sanchari.user.mapper.UserMapper;
import com.sanchari.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal SanchariUserPrincipal p) {
        return UserMapper.toDto(userService.getById(p.userId()));
    }

    @PutMapping("/me")
    public UserDto update(@AuthenticationPrincipal SanchariUserPrincipal p, @Valid @RequestBody UpdateProfileRequest req) {
        return UserMapper.toDto(userService.updateProfile(p.userId(), req));
    }
}
