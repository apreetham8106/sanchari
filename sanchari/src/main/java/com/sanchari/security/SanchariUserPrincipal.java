package com.sanchari.security;

import com.sanchari.common.enums.Role;
import java.security.Principal;
import java.util.UUID;

/** The authenticated caller. getName() is the user id, which is also the STOMP user name. */
public record SanchariUserPrincipal(UUID userId, Role role) implements Principal {
    @Override
    public String getName() {
        return userId.toString();
    }
}
