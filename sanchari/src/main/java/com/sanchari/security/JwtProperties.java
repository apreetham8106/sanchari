package com.sanchari.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "sanchari.jwt")
public record JwtProperties(String secret,
                            @DefaultValue("15") long accessTtlMinutes,
                            @DefaultValue("30") long refreshTtlDays) {
}
