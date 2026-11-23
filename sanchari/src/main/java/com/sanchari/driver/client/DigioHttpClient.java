package com.sanchari.driver.client;

import com.sanchari.common.exception.SanchariException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Real Digio client (Basic auth with client id and secret).
 * The endpoint paths (sanchari.digio.driver-path / vehicle-path) and request bodies below are PLACEHOLDERS:
 * check them against the Digio products enabled on your account before going live.
 */
@Component
@ConditionalOnProperty(name = "sanchari.digio.mode", havingValue = "http")
public class DigioHttpClient implements DigioClient {

    private static final Logger log = LoggerFactory.getLogger(DigioHttpClient.class);

    private final DigioProperties props;
    private final RestClient client;

    public DigioHttpClient(DigioProperties props) {
        this.props = props;
        this.client = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Basic " + HttpHeaders.encodeBasicAuth(props.clientId(), props.clientSecret(), StandardCharsets.UTF_8))
                .build();
    }

    @Override
    public String startDriverVerification(UUID driverId, String licenseNumber, LocalDate dateOfBirth) {
        return post(props.driverPath(), Map.of(
                "customer_identifier", driverId.toString(),
                "dl_number", licenseNumber,
                "dob", dateOfBirth.toString()));
    }

    @Override
    public String startVehicleVerification(UUID driverId, String registrationNo) {
        return post(props.vehiclePath(), Map.of(
                "customer_identifier", driverId.toString(),
                "rc_number", registrationNo));
    }

    @SuppressWarnings("unchecked")
    private String post(String path, Map<String, Object> body) {
        try {
            Map<String, Object> resp = client.post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            Object id = resp == null ? null : resp.getOrDefault("id", resp.get("request_id"));
            if (id == null) {
                throw new SanchariException(HttpStatus.BAD_GATEWAY, "Verification provider returned no request id");
            }
            return id.toString();
        } catch (RestClientException e) {
            log.error("Digio call failed: {}", e.getMessage());
            throw new SanchariException(HttpStatus.BAD_GATEWAY, "Verification provider is unavailable. Try again later.");
        }
    }
}
