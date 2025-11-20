package com.assistent.draftly.repository;

import com.assistent.draftly.entity.GmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.OptionalInt;

@Repository
public interface GmailTokenRepository extends JpaRepository<GmailToken, Long> {
    Optional<GmailToken> findByEmail(String email);
    Optional<GmailToken> findByGoogleUserId(String googleUserId);
}