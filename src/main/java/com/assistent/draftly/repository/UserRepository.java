package com.assistent.draftly.repository;

import com.assistent.draftly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByGoogleUserId(String googleUserId);
}