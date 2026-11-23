package com.anvay.notification.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "anvay.sms")
public record SmsProperties(@DefaultValue("console") String provider, Http http) {

    public record Http(@DefaultValue("") String url,
                       @DefaultValue("") String apiKey,
                       @DefaultValue("ANVAY") String senderId) {
    }
}
