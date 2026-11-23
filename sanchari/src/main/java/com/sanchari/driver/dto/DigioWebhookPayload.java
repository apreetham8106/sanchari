package com.sanchari.driver.dto;

import java.util.Map;

/** Normalised view of Digio's callback. Adapt the extraction in DriverKycWebhookController to your account's payload. */
public record DigioWebhookPayload(String requestId, String status, Map<String, Object> raw) {
}
