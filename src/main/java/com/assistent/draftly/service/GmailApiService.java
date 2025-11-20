package com.assistent.draftly.service;

import com.assistent.draftly.entity.GmailToken;
import com.assistent.draftly.repository.GmailTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GmailApiService {
    private final WebClient webClient;
    private final GmailOAuthService oauthService;
    private final GmailTokenRepository repo;

    @Value("${app.google.gmail-api-base}")
    private String gmailBase;

    public GmailApiService(WebClient webClient,
                           GmailOAuthService oauthService,
                           GmailTokenRepository repo) {

        this.webClient = webClient;
        this.oauthService = oauthService;
        this.repo = repo;
    }

    public Mono<List<Map<String, Object>>> listUnreadMessages(String email) {
        Optional<GmailToken> opt = repo.findByEmail(email);
        if (opt.isEmpty()) return Mono.just(List.of());

        GmailToken token = opt.get();

        return ensureValidAccessToken(token)
                .flatMap(accessToken ->
                        webClient.get()
                                .uri(gmailBase + "/users/me/messages?q=is:unread&maxResults=1")
                                .headers(h -> h.setBearerAuth(accessToken))
                                .retrieve()
                                .bodyToMono(Map.class)
                )
                .map(response -> {
                    Object messagesObj = response.get("messages");

                    if (messagesObj instanceof List<?> list) {
                        return (List<Map<String, Object>>) (List<?>) list;
                    }

                    return List.<Map<String, Object>>of();
                });
    }

    public Mono<Void> markAsRead(String email, String messageId) {
        Optional<GmailToken> opt = repo.findByEmail(email);
        if (opt.isEmpty()) return Mono.empty();

        GmailToken token = opt.get();
        return ensureValidAccessToken(token).flatMap(accessToken -> webClient.post()
                .uri(gmailBase + "/users/me/messages/{id}/modify", messageId)
                .headers(h -> h.setBearerAuth(accessToken))
                .bodyValue(Map.of("removeLabelIds", List.of("UNREAD")))
                .retrieve()
                .bodyToMono(Void.class));
    }

    // ensure token is valid — if expired, refresh and update DB
    private Mono<String> ensureValidAccessToken(GmailToken token) {
        if (token.getAccessToken() != null && token.getAccessTokenExpiryAt() != null
                && token.getAccessTokenExpiryAt().isAfter(Instant.now().plusSeconds(30))) {
            return Mono.just(token.getAccessToken());
        }
        // refresh
        return oauthService.refreshAccessToken(token.getRefreshToken())
                .map(resp -> {
                    String newAccessToken = (String) resp.get("access_token");
                    Integer expiresIn = resp.get("expires_in") == null ? null : ((Number) resp.get("expires_in")).intValue();
                    token.setAccessToken(newAccessToken);
                    if (expiresIn != null) token.setAccessTokenExpiryAt(Instant.now().plusSeconds(expiresIn));
                    repo.save(token);
                    return newAccessToken;
                });
    }

    // Optional cron: poll unread messages for all stored accounts
    @Scheduled(cron = "${poll.unread-check-cron}")
    public void pollUnreadForAll() {
        List<GmailToken> tokens = repo.findAll();
        for (GmailToken t : tokens) {
            listUnreadMessages(t.getEmail()).subscribe(list -> {
                if (list.isEmpty()) {
                    System.out.println("No unread for " + t.getEmail());
                } else {
                    System.out.println("Found " + list.size() + " unread for " + t.getEmail());
                    // optionally mark first as read for demo:
                    Map first = list.get(0);
                    String msgId = (String) first.get("id");
                    markAsRead(t.getEmail(), msgId).subscribe();
                }
            }, err -> {
                System.err.println("Error fetching for " + t.getEmail() + " -> " + err.getMessage());
            });
        }
    }

    public Mono<List<Map<String, Object>>> listUnreadMessagesWithDetails(String email) {

        Optional<GmailToken> opt = repo.findByEmail(email);
        if (opt.isEmpty()) return Mono.just(List.of());

        GmailToken token = opt.get();

        return ensureValidAccessToken(token)
                .flatMap(accessToken -> {

                    return webClient.get()
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
                            });
                });
    }


}
