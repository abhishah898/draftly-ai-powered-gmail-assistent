package com.assistent.draftly.repository;

import com.assistent.draftly.entity.GoogleToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleTokenRepository extends JpaRepository<GoogleToken, String> {

    Optional<GoogleToken> findByEmail(String email);

    Optional<GoogleToken> findByGoogleUserId(String googleUserId);
}