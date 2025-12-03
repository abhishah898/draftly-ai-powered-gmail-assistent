package com.assistent.draftly.service;

import com.assistent.draftly.entity.GmailToken;
import com.assistent.draftly.repository.GmailTokenRepository;
import com.assistent.draftly.security.TokenEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class GmailApiService {
    private final WebClient webClient;
    private final GmailOAuthService oauthService;
    private final GmailTokenRepository repo;
    private final TokenEncryptor tokenEncryptor;

    @Value("${app.google.gmail-api-base}")
    private String gmailBase;

    public GmailApiService(WebClient webClient,
                           GmailOAuthService oauthService,
                           GmailTokenRepository repo,
                           TokenEncryptor tokenEncryptor) {

        this.webClient = webClient;
        this.oauthService = oauthService;
        this.repo = repo;
        this.tokenEncryptor = tokenEncryptor;
    }

    // ensure token is valid — if expired, refresh and update DB
    private Mono<String> ensureValidAccessToken(GmailToken token) {
        // Decrypt stored tokens before using
        String decryptedAccessToken =
                tokenEncryptor.decryptToken(token.getAccessToken());

        // if unexpired => return decrypted token
        if (decryptedAccessToken != null
                && token.getAccessTokenExpiryAt() != null
                && token.getAccessTokenExpiryAt().isAfter(Instant.now().plusSeconds(30))) {
            return Mono.just(decryptedAccessToken);
        }

        // expired => use refresh token
        // decrypted refresh token before calling Google
        String decryptedRefreshToken =
                tokenEncryptor.decryptToken(token.getRefreshToken());

        return oauthService.refreshAccessToken(decryptedRefreshToken)
                .flatMap(resp -> {
                    String newAccessToken = (String) resp.get("access_token");
                    Integer expiresIn = resp.get("expires_in") == null
                            ? null
                            : ((Number) resp.get("expires_in")).intValue();

                    // Store encrypted token back to DB
                    token.setAccessToken(tokenEncryptor.encryptToken(newAccessToken));

                    if (expiresIn != null)
                        token.setAccessTokenExpiryAt(Instant.now().plusSeconds(expiresIn));

                    return repo.save(token)
                            .thenReturn(newAccessToken); // return decrypted access token
                });
    }

    public Mono<List<Map<String, Object>>> listUnreadMessagesWithDetails(String email) {
        return repo.findByEmail(email)
                .flatMap(token ->
                        ensureValidAccessToken(token)
                                .flatMap(accessToken ->
                                        webClient.get()
                                                .uri(gmailBase + "/users/me/messages?q=is:unread")
                                                .headers(h -> h.setBearerAuth(accessToken))
                                                .retrieve()
                                                .bodyToMono(Map.class)
                                                .flatMap(resp -> {

                                                    List<Map<String, Object>> messages =
                                                            (List<Map<String, Object>>) resp.getOrDefault("messages", List.of());

                                                    return Flux.fromIterable(messages)
                                                            .flatMap(msg ->
                                                                    webClient.get()
                                                                            .uri(gmailBase + "/users/me/messages/{id}?format=metadata", msg.get("id"))
                                                                            .headers(h -> h.setBearerAuth(accessToken))
                                                                            .retrieve()
                                                                            .bodyToMono(Map.class)
                                                                            .map(m -> (Map<String, Object>) m)
                                                            )
                                                            .collectList();
                                                })
                                )
                )
                .switchIfEmpty(Mono.just(List.of()));  // when no GmailToken
    }
}