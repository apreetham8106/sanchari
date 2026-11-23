package com.anvay.dispatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "anvay.dispatch")
public record DispatchProperties(@DefaultValue("1000") int startRadiusM,
                                 @DefaultValue("1000") int stepM,
                                 @DefaultValue("5000") int maxRadiusM,
                                 @DefaultValue("15") int offerTtlSeconds,
                                 @DefaultValue("5") int batchSize,
                                 @DefaultValue("120") int searchTimeoutSeconds,
                                 @DefaultValue("30") int locationFreshnessSeconds,
                                 @DefaultValue("3000") long tickMs) {
}
