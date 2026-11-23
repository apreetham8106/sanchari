package com.anvay.notification.sms;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Generic HTTP SMS gateway. The request shape (JSON {to, message, sender}) and the bearer API key are a
 * placeholder: adapt send() to your provider's API (and, for India, its DLT-registered template rules).
 */
@Component
@ConditionalOnProperty(name = "anvay.sms.provider", havingValue = "http")
public class ThirdPartySmsGateway implements SmsGateway {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartySmsGateway.class);

    private final SmsProperties props;
    private final RestClient client;

    public ThirdPartySmsGateway(SmsProperties props) {
        this.props = props;
        this.client = RestClient.builder().build();
    }

    @Override
    public void send(String phone, String message) {
        SmsProperties.Http cfg = props.http();
        if (cfg == null || cfg.url().isBlank()) {
            throw new IllegalStateException("anvay.sms.http.url is not configured");
        }
        client.post()
                .uri(cfg.url())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + cfg.apiKey())
                .body(Map.of("to", phone, "message", message, "sender", cfg.senderId()))
                .retrieve()
                .toBodilessEntity();
        log.info("SMS sent to {}", phone);
    }
}
