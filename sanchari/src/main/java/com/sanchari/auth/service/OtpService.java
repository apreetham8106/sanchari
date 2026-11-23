package com.sanchari.auth.service;

import com.sanchari.auth.domain.OtpCode;
import com.sanchari.auth.domain.OtpPurpose;
import com.sanchari.auth.kafka.OtpEventProducer;
import com.sanchari.auth.repository.OtpCodeRepository;
import com.sanchari.common.exception.SanchariException;
import com.sanchari.common.exception.TooManyRequestsException;
import com.sanchari.common.exception.UnauthorizedException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCodeRepository repo;
    private final OtpProperties props;
    private final PasswordEncoder encoder;
    private final OtpEventProducer producer;

    public OtpService(OtpCodeRepository repo, OtpProperties props, PasswordEncoder encoder, OtpEventProducer producer) {
        this.repo = repo;
        this.props = props;
        this.encoder = encoder;
        this.producer = producer;
    }

    @Transactional
    public void send(String phone, OtpPurpose purpose) {
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(props.windowMinutes()));
        if (repo.countByPhoneAndCreatedAtAfter(phone, windowStart) >= props.maxRequestsPerWindow()) {
            throw new TooManyRequestsException("Too many OTP requests. Try again in a few minutes.");
        }
        String code = props.devFixedCode() != null && !props.devFixedCode().isBlank()
                ? props.devFixedCode()
                : generate();

        OtpCode otp = new OtpCode();
        otp.setPhone(phone);
        otp.setPurpose(purpose);
        otp.setCodeHash(encoder.encode(code));
        otp.setExpiresAt(Instant.now().plus(Duration.ofMinutes(props.ttlMinutes())));
        repo.save(otp);

        producer.publish(phone, code, purpose);
    }

    /**
     * Verifies and consumes the latest OTP. Failed attempts must be persisted, so a failure
     * (an SanchariException) does not roll the transaction back.
     */
    @Transactional(noRollbackFor = SanchariException.class)
    public void verify(String phone, String code, OtpPurpose purpose) {
        OtpCode otp = repo.findTopByPhoneAndPurposeAndConsumedFalseOrderByCreatedAtDesc(phone, purpose)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));
        if (otp.getExpiresAt().isBefore(Instant.now()) || otp.getAttempts() >= props.maxAttempts()) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }
        otp.setAttempts(otp.getAttempts() + 1);
        if (!encoder.matches(code, otp.getCodeHash())) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }
        otp.setConsumed(true);
    }

    private String generate() {
        int bound = (int) Math.pow(10, props.length());
        return String.format("%0" + props.length() + "d", RANDOM.nextInt(bound));
    }
}
