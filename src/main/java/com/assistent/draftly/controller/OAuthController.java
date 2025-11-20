package com.assistent.draftly.controller;

import com.assistent.draftly.service.GmailApiService;
import com.assistent.draftly.service.GmailOAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class OAuthController {

    private final GmailOAuthService oauthService;
    private final GmailApiService gmailApiService;

    public OAuthController(GmailOAuthService oauthService, GmailApiService gmailApiService) {
        this.oauthService = oauthService;
        this.gmailApiService = gmailApiService;
    }

    private String findHeader(List<Map<String, String>> headers, String name) {
        return headers.stream()
                .filter(h -> h.get("name").equalsIgnoreCase(name))
                .map(h -> h.get("value"))
                .findFirst()
                .orElse("N/A");
    }
    private String extractBody(Map msg) {
        try {
            Map payload = (Map) msg.get("payload");

            // Case 1: simple email
            if (payload.containsKey("body")) {
                Map body = (Map) payload.get("body");
                String data = (String) body.get("data");
                if (data != null) {
                    return new String(Base64.getUrlDecoder().decode(data));
                }
            }

            // Case 2: multipart email
            List<Map> parts = (List<Map>) payload.get("parts");
            if (parts != null) {
                for (Map p : parts) {
                    Map body = (Map) p.get("body");
                    if (body != null && body.get("data") != null) {
                        return new String(Base64.getUrlDecoder().decode((String) body.get("data")));
                    }
                }
            }

        } catch (Exception ignored) {}

        return "(No Content)";
    }

    // Hit this from browser to start flow
    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestParam(value = "state", required = false) String state) {
        URI uri = oauthService.buildAuthorizationUrl(state == null ? "app" : state);
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, uri.toString()).build();
    }

    // Google will redirect here with ?code=...&state=...
    @GetMapping("/callback")
    public Mono<String> callback(@RequestParam("code") String code,
                                 @RequestParam(value = "state", required = false) String state) {

        return oauthService.exchangeCodeForToken(code)
                .flatMap(tokenResponse -> {

                    String accessToken = (String) tokenResponse.get("access_token");
                    String refreshToken = (String) tokenResponse.get("refresh_token");
                    Integer expiresIn = tokenResponse.get("expires_in") == null
                            ? null
                            : ((Number) tokenResponse.get("expires_in")).intValue();

                    return oauthService.getUserInfoFromAccessToken(accessToken)
                            .flatMap(profile -> {

                                String email = (String) profile.get("email");
                                String sub = (String) profile.get("id");

                                oauthService.upsertToken(sub, email, refreshToken, accessToken, expiresIn);

                                // Fetch UNREAD messages with FULL details
                                return gmailApiService.listUnreadMessagesWithDetails(email)
                                        .map(messages -> {

                                            StringBuilder sb = new StringBuilder();
                                            sb.append("<h2>Connected Gmail: ").append(email).append("</h2><br>");

                                            if (messages.isEmpty()) {
                                                sb.append("<h3>No unread emails 🎉</h3>");
                                                return sb.toString();
                                            }

                                            sb.append("<h3>Unread Emails</h3><hr>");

                                            for (Map msg : messages) {
                                                sb.append("<div style='margin-bottom:20px;'>");

                                                sb.append("<b>Message ID:</b> ").append(msg.get("id")).append("<br>");
                                                sb.append("<b>Thread ID:</b> ").append(msg.get("threadId")).append("<br>");
                                                sb.append("<b>Snippet:</b> ").append(msg.get("snippet")).append("<br>");

                                                // Extract headers
                                                List<Map<String, String>> headers =
                                                        (List<Map<String, String>>) ((Map) msg.get("payload")).get("headers");

                                                String subject = findHeader(headers, "Subject");
                                                String from = findHeader(headers, "From");
                                                String to = findHeader(headers, "To");
                                                String date = findHeader(headers, "Date");

                                                sb.append("<b>Subject:</b> ").append(subject).append("<br>");
                                                sb.append("<b>From:</b> ").append(from).append("<br>");
                                                sb.append("<b>To:</b> ").append(to).append("<br>");
                                                sb.append("<b>Date:</b> ").append(date).append("<br>");

                                                // Extract Body
                                                String body = extractBody(msg);
                                                sb.append("<b>Body:</b><br><pre>").append(body).append("</pre>");

                                                sb.append("<hr></div>");
                                            }

                                            return sb.toString();
                                        });
                            });
                });
    }
}
