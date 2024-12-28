package com.example.hotelmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hotelmanager.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
	Optional<User> findByVerificationToken(String token);
	Optional<User> findByResetToken(String resetToken);

}
