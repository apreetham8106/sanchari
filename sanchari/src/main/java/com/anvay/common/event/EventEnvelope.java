package com.anvay.common.event;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(UUID eventId, String type, Instant occurredAt, T payload) {
}
