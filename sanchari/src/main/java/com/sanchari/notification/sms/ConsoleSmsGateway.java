package com.sanchari.notification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Dev gateway: prints the SMS in the backend log instead of sending it. */
@Component
@ConditionalOnProperty(name = "sanchari.sms.provider", havingValue = "console", matchIfMissing = true)
public class ConsoleSmsGateway implements SmsGateway {

    private static final Logger log = LoggerFactory.getLogger(ConsoleSmsGateway.class);

    @Override
    public void send(String phone, String message) {
        log.info("[SMS -> {}] {}", phone, message);
    }
}
