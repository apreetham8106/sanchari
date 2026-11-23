package com.anvay.auth.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "anvay.otp")
public record OtpProperties(@DefaultValue("6") int length,
                            @DefaultValue("5") long ttlMinutes,
                            @DefaultValue("5") int maxAttempts,
                            @DefaultValue("3") int maxRequestsPerWindow,
                            @DefaultValue("10") long windowMinutes,
                            @DefaultValue("") String devFixedCode) {
}
