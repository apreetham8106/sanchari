package com.sanchari.user.service;

import com.sanchari.common.enums.Role;
import com.sanchari.common.exception.ConflictException;
import com.sanchari.common.exception.NotFoundException;
import com.sanchari.user.domain.User;
import com.sanchari.user.domain.UserStatus;
import com.sanchari.user.dto.UpdateProfileRequest;
import com.sanchari.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return users.findByPhone(phone);
    }

    @Transactional
    public User updateProfile(UUID id, UpdateProfileRequest req) {
        User u = getById(id);
        String email = req.email() == null || req.email().isBlank() ? null : req.email().trim().toLowerCase();
        if (email != null && !email.equals(u.getEmail()) && users.existsByEmail(email)) {
            throw new ConflictException("Email already in use");
        }
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        return u;
    }

    @Transactional
    public User setStatus(UUID id, UserStatus status) {
        User u = getById(id);
        u.setStatus(status);
        return u;
    }

    @Transactional(readOnly = true)
    public Page<User> search(Role role, String q, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (role != null) {
                ps.add(cb.equal(root.get("role"), role));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.add(cb.or(cb.like(cb.lower(root.get("fullName")), like), cb.like(root.get("phone"), like)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return users.findAll(spec, pageable);
    }
}
