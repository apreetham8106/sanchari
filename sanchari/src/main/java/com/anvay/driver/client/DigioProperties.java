package com.anvay.driver.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "anvay.digio")
public record DigioProperties(@DefaultValue("stub") String mode,
                              @DefaultValue("") String baseUrl,
                              @DefaultValue("") String clientId,
                              @DefaultValue("") String clientSecret,
                              @DefaultValue("") String webhookSecret,
                              @DefaultValue("") String driverPath,
                              @DefaultValue("") String vehiclePath) {
}
