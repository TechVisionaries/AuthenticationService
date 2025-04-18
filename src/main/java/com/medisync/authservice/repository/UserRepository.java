package com.medisync.authservice.repository;

import com.medisync.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//user repo
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
