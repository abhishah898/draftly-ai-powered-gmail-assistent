package com.assistent.draftly.service;

import com.assistent.draftly.dto.GoogleTokenResponse;
import com.assistent.draftly.dto.GoogleUserProfile;
import com.assistent.draftly.entity.GoogleToken;
import com.assistent.draftly.repository.GoogleTokenRepository;
import com.assistent.draftly.security.TokenEncryptor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final GoogleTokenRepository gmailTokenRepository;
    private final TokenEncryptor tokenEncryptor;
    private final WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.token-uri}")
    private String tokenUri;

    public String getValidAccessToken(String googleUserId) {
        GoogleToken token = gmailTokenRepository.findByGoogleUserId(googleUserId)
                .orElseThrow(() ->
                        new IllegalStateException("No stored token for user: " + googleUserId));

        String decryptedAccessToken = tokenEncryptor.decryptToken(token.getAccessToken());

        if (token.getAccessTokenExpiryAt() != null &&
                token.getAccessTokenExpiryAt().isAfter(Instant.now().plusSeconds(30))) {
            return decryptedAccessToken;
        }

        return refreshAccessToken(token);
    }

    private String refreshAccessToken(GoogleToken token) {
        String decryptedRefreshToken = tokenEncryptor.decryptToken(token.getRefreshToken());

        if (decryptedRefreshToken == null || decryptedRefreshToken.isBlank()) {
            throw new IllegalStateException("No refresh token found — cannot refresh access!");
        }

        GoogleTokenResponse response = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("refresh_token=" + decryptedRefreshToken +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=refresh_token")
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        if (response == null || response.getAccessToken() == null) {
            throw new IllegalStateException("Token refresh failed!");
        }

        token.setAccessToken(tokenEncryptor.encryptToken(response.getAccessToken()));
        token.setAccessTokenExpiryAt(Instant.now().plusSeconds(response.getExpiresIn()));

        gmailTokenRepository.save(token);

        log.info("Refreshed access token for user {}", token.getGoogleUserId());

        return response.getAccessToken();
    }

    @Transactional
    public void saveOrUpdateToken(GoogleUserProfile profile, GoogleTokenResponse tokenResp) {

        GoogleToken token = gmailTokenRepository.findByGoogleUserId(profile.getId())
                .orElse(new GoogleToken());

        token.setGoogleUserId(profile.getId());
        token.setEmail(profile.getEmail());

        if (tokenResp.getRefreshToken() != null) {
            token.setRefreshToken(tokenEncryptor.encryptToken(tokenResp.getRefreshToken()));
        }

        token.setAccessToken(tokenEncryptor.encryptToken(tokenResp.getAccessToken()));

        if (tokenResp.getExpiresIn() != null)
            token.setAccessTokenExpiryAt(Instant.now().plusSeconds(tokenResp.getExpiresIn()));

        gmailTokenRepository.save(token);
    }

}