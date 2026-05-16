package com.stockpro.auth.repository;

import com.stockpro.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);

    List<User> findAllByRole(String role);

    List<User> findAllByRoleIn(Collection<String> roles);

    List<User> findByDepartment(String department);

    List<User> findByIsActive(boolean isActive);

    void deleteByUserId(Long userId);
}
