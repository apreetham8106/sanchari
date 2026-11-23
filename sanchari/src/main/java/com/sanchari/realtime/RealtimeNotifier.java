package com.sanchari.realtime;

import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Thin wrapper over the STOMP template so the rest of the code never builds destinations by hand twice. */
@Component
public class RealtimeNotifier {

    private final SimpMessagingTemplate template;

    public RealtimeNotifier(SimpMessagingTemplate template) {
        this.template = template;
    }

    /** Delivered to the client subscribed to /user{destination}, e.g. destination "/queue/offers". */
    public void toUser(UUID userId, String destination, Object payload) {
        template.convertAndSendToUser(userId.toString(), destination, payload);
    }

    public void toTopic(String destination, Object payload) {
        template.convertAndSend(destination, payload);
    }
}
