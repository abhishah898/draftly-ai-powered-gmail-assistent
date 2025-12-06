package com.assistent.draftly.controller;

import com.assistent.draftly.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Returns currently logged-in user
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        return userService.getCurrentUser(session)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body(
                        Map.of("error", "Not logged in")
                ));
    }

    // Logs out user
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(WebSession webSession) {
        return webSession.invalidate()
                .thenReturn(ResponseEntity.ok().build());
    }
}