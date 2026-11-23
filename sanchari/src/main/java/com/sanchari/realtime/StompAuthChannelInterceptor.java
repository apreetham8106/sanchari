package com.sanchari.realtime;

import com.sanchari.common.enums.Role;
import com.sanchari.ride.service.RideService;
import com.sanchari.security.SanchariUserPrincipal;
import com.sanchari.security.JwtService;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Authenticates the STOMP CONNECT frame with the same JWT as the REST API (header "Authorization: Bearer ..."),
 * and authorises SUBSCRIBE frames: ride topics only for the ride's rider/driver (or an admin), admin topics only
 * for admins. /user/** destinations are private to the connected user by construction.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern RIDE_TOPIC = Pattern.compile("^/topic/rides/([0-9a-fA-F\\-]{36})(/.*)?$");

    private final JwtService jwt;
    private final RideService rideService;

    // @Lazy breaks a startup cycle: broker channel -> this interceptor -> RideService -> notifier -> broker.
    public StompAuthChannelInterceptor(JwtService jwt, @Lazy RideService rideService) {
        this.jwt = jwt;
        this.rideService = rideService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null || acc.getCommand() == null) {
            return message;
        }
        if (acc.getCommand() == StompCommand.CONNECT) {
            String header = acc.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                throw new MessagingException("Missing token");
            }
            SanchariUserPrincipal principal = jwt.parse(header.substring(7))
                    .orElseThrow(() -> new MessagingException("Invalid token"));
            acc.setUser(new UsernamePasswordAuthenticationToken(principal, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))));
        } else if (acc.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscription(acc);
        }
        return message;
    }

    private void authorizeSubscription(StompHeaderAccessor acc) {
        String destination = acc.getDestination();
        if (destination == null) {
            throw new MessagingException("Missing destination");
        }
        if (!(acc.getUser() instanceof UsernamePasswordAuthenticationToken token)
                || !(token.getPrincipal() instanceof SanchariUserPrincipal principal)) {
            throw new MessagingException("Not authenticated");
        }
        if (destination.startsWith("/user/")) {
            return;
        }
        if (destination.startsWith("/topic/admin")) {
            if (principal.role() != Role.ADMIN) {
                throw new MessagingException("Forbidden");
            }
            return;
        }
        Matcher m = RIDE_TOPIC.matcher(destination);
        if (m.matches()) {
            if (!rideService.isParticipant(UUID.fromString(m.group(1)), principal.userId(), principal.role())) {
                throw new MessagingException("Forbidden");
            }
            return;
        }
        throw new MessagingException("Unknown destination");
    }
}
