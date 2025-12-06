package com.assistent.draftly.service;

import com.assistent.draftly.dto.GoogleTokenResponse;
import com.assistent.draftly.dto.GoogleUserProfile;
import com.assistent.draftly.entity.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class GoogleOAuthService {
    private final WebClient webClient;

    private final UserService userService;

    private final TokenService tokenService;

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthService.class);

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.redirect-uri}")
    private String redirectUri;

    @Value("${app.google.auth-uri}")
    private String authUri;

    @Value("${app.google.token-uri}")
    private String tokenUri;

    @Value("${app.google.scopes}")
    private String scopes;

    @Value("${app.google.gmail-api-base}")
    private String gmailBase;

    public GoogleOAuthService(
            UserService userService
            , WebClient webClient
            , TokenService tokenService
    ) {
        this.webClient = webClient;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public URI buildAuthorizationUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl(authUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scopes)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .encode()
                .toUri();
    }

    public GoogleTokenResponse exchangeCodeForToken(String code) {
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("code=" + code +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&redirect_uri=" + redirectUri +
                        "&grant_type=authorization_code")
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();
    }

    @Transactional
    public User handleOAuthCallback(String code) {

        GoogleTokenResponse tokenResp = exchangeCodeForToken(code);
        if (tokenResp == null || tokenResp.getAccessToken() == null) {
            throw new IllegalArgumentException("Failed to exchange auth code for token");
        }

        GoogleUserProfile profile =
                userService.getUserInfoFromAccessToken(tokenResp.getAccessToken());

        if (profile == null || profile.getId() == null) {
            throw new IllegalStateException("Failed to fetch user profile from Google");
        }

        User user = userService.saveOrUpdateUser(profile);
        tokenService.saveOrUpdateToken(profile, tokenResp);

        log.info("User Authenticated: {}", user.getEmail());
        return user;
    }

}