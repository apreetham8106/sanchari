package com.sanchari.driver.api;

import com.sanchari.common.exception.BadRequestException;
import com.sanchari.common.exception.UnauthorizedException;
import com.sanchari.driver.client.DigioProperties;
import com.sanchari.driver.dto.DigioWebhookPayload;
import com.sanchari.driver.service.DriverKycService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Digio callback. The signature header name (X-Digio-Signature, hex HMAC-SHA256 of the raw body) and the
 * payload field names are assumptions: confirm both in your Digio account's webhook documentation.
 */
@RestController
@RequestMapping("/api/webhooks/digio")
public class DriverKycWebhookController {

    private final DigioProperties props;
    private final DriverKycService kycService;
    private final ObjectMapper mapper;

    public DriverKycWebhookController(DigioProperties props, DriverKycService kycService, ObjectMapper mapper) {
        this.props = props;
        this.kycService = kycService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody String body,
                                        @RequestHeader(value = "X-Digio-Signature", required = false) String signature) {
        verifySignature(body, signature);
        DigioWebhookPayload payload = parse(body);
        if (payload.requestId() == null) {
            throw new BadRequestException("Missing request id");
        }
        kycService.applyResult(payload.requestId(), payload.status(), payload.raw());
        return ResponseEntity.ok().build();
    }

    private void verifySignature(String body, String signature) {
        String secret = props.webhookSecret();
        if (secret == null || secret.isBlank()) {
            if ("stub".equalsIgnoreCase(props.mode())) {
                return; // dev only
            }
            throw new UnauthorizedException("Webhook secret is not configured");
        }
        if (signature == null) {
            throw new UnauthorizedException("Missing signature");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
                throw new UnauthorizedException("Invalid signature");
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid signature");
        }
    }

    private DigioWebhookPayload parse(String body) {
        try {
            JsonNode root = mapper.readTree(body);
            JsonNode inner = root.has("payload") && root.get("payload").isObject() ? root.get("payload") : root;
            String requestId = text(inner, "id", "request_id", "requestId");
            if (requestId == null) {
                requestId = text(root, "id", "request_id", "requestId");
            }
            String status = text(inner, "status", "state");
            if (status == null) {
                status = text(root, "status", "state");
            }
            Map<String, Object> raw = mapper.convertValue(root, new TypeReference<Map<String, Object>>() {
            });
            return new DigioWebhookPayload(requestId, status, raw);
        } catch (Exception e) {
            throw new BadRequestException("Invalid webhook payload");
        }
    }

    private static String text(JsonNode node, String... names) {
        for (String n : names) {
            JsonNode v = node.get(n);
            if (v != null && !v.isNull() && v.isValueNode()) {
                return v.asText();
            }
        }
        return null;
    }
}
