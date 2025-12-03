package com.assistent.draftly.service;

import com.assistent.draftly.entity.GmailToken;
import com.assistent.draftly.repository.GmailTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Service
public class GmailOAuthService {
    private final WebClient webClient;

    private final GmailTokenRepository gmailTokenRepository;

    @Value("${app.google.client-id}")
    private String id;

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

    public GmailOAuthService(GmailTokenRepository repo) {
        this.webClient = WebClient.builder()
                .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10 MB
                .build();
        this.gmailTokenRepository = repo;
    }

    public Mono<Map> getUserInfoFromAccessToken(String accessToken) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class);
    }


    public URI buildAuthorizationUrl(String state) {
        URI uri = UriComponentsBuilder.fromHttpUrl(authUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scopes)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .encode()                 // <-- ensure spaces are percent-encoded
                .toUri();

        return uri;
    }

    public Mono<Map> exchangeCodeForToken(String code) {
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("code=" + code +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&redirect_uri=" + redirectUri +
                        "&grant_type=authorization_code")
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> refreshAccessToken(String refreshToken) {
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("refresh_token=" + refreshToken +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=refresh_token")
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<GmailToken> upsertToken(
            String googleUserId,
            String email,
            String refreshToken,
            String accessToken,
            Integer expiresInSeconds
    ) {
        return gmailTokenRepository.findByGoogleUserId(googleUserId)
                .defaultIfEmpty(new GmailToken())
                .flatMap(token -> {
                    token.setGoogleUserId(googleUserId);
                    token.setEmail(email);

                    // check if refresh token expired
                    if (refreshToken != null && !refreshToken.isBlank())
                        token.setRefreshToken(refreshToken);

                    token.setAccessToken(accessToken);

                    if (expiresInSeconds != null)
                        token.setAccessTokenExpiryAt(
                                Instant.now().plusSeconds(expiresInSeconds)
                        );
                    return gmailTokenRepository.save(token);
                });
    }
}