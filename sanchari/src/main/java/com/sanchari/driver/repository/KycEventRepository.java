package com.sanchari.driver.repository;

import com.sanchari.driver.domain.KycEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycEventRepository extends JpaRepository<KycEvent, Long> {
    List<KycEvent> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
