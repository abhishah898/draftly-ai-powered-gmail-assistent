package com.assistent.draftly.controller;

import com.assistent.draftly.entity.User;
import com.assistent.draftly.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class GoogleOAuthController {

    private final GoogleOAuthService oauthService;

    public GoogleOAuthController(GoogleOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(
            @RequestParam(value = "state", required = false) String state
    ) {
        URI uri = oauthService.buildAuthorizationUrl(state == null ? "app" : state);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, uri.toString())
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpSession session
    ) {
        User user = oauthService.handleOAuthCallback(code);

        // Prevent Session Fixation
        session.invalidate();

        // Create a new secure session
        HttpSession newSession = request.getSession(true);

        newSession.setAttribute("userId", user.getId());
        newSession.setAttribute("email", user.getEmail());

        Map<String, Object> resp = Map.of(
                "Message", "Login Successful",
                "email", user.getEmail()
        );

        return ResponseEntity.ok(resp);
    }

}