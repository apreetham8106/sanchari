package com.anvay.user.repository;

import com.anvay.common.enums.Role;
import com.anvay.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    long countByRole(Role role);
}
