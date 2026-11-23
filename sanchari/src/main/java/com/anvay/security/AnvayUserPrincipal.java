package com.anvay.security;

import com.anvay.common.enums.Role;
import java.security.Principal;
import java.util.UUID;

/** The authenticated caller. getName() is the user id, which is also the STOMP user name. */
public record AnvayUserPrincipal(UUID userId, Role role) implements Principal {
    @Override
    public String getName() {
        return userId.toString();
    }
}
