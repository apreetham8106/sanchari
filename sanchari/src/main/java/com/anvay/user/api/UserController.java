package com.anvay.user.api;

import com.anvay.security.AnvayUserPrincipal;
import com.anvay.user.dto.UpdateProfileRequest;
import com.anvay.user.dto.UserDto;
import com.anvay.user.mapper.UserMapper;
import com.anvay.user.service.UserService;
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
    public UserDto me(@AuthenticationPrincipal AnvayUserPrincipal p) {
        return UserMapper.toDto(userService.getById(p.userId()));
    }

    @PutMapping("/me")
    public UserDto update(@AuthenticationPrincipal AnvayUserPrincipal p, @Valid @RequestBody UpdateProfileRequest req) {
        return UserMapper.toDto(userService.updateProfile(p.userId(), req));
    }
}
