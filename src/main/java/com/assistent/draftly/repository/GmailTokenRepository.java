package com.assistent.draftly.repository;

import com.assistent.draftly.entity.GmailToken;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface GmailTokenRepository extends R2dbcRepository<GmailToken, Long> {
    Mono<GmailToken> findByEmail(String email);
    Mono<GmailToken> findByGoogleUserId(String googleUserId);
}