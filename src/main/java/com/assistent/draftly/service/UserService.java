package com.assistent.draftly.service;

import com.assistent.draftly.dto.GoogleUserProfile;
import com.assistent.draftly.entity.User;
import com.assistent.draftly.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final WebClient webClient;

    private final UserRepository userRepository;

    public UserService(WebClient webClient,
                       UserRepository userRepository) {
        this.webClient = webClient;
        this.userRepository = userRepository;
    }

    public GoogleUserProfile getUserInfoFromAccessToken(String accessToken) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleUserProfile.class)
                .block(); // Sync execution 👍
    }

    @Transactional
    public User saveOrUpdateUser(GoogleUserProfile profile) {
        User user = userRepository.findByGoogleUserId(profile.getId())
                .orElse(new User());

        user.setGoogleUserId(profile.getId());
        user.setEmail(profile.getEmail());
        user.setFirstName(profile.getFirstName());
        user.setLastName(profile.getLastName());
        user.setProfilePictureURL(profile.getPicture());

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }

        return userRepository.save(user);
    }

    public Optional<Map<String, Object>> getCurrentUser(HttpSession session) {

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return Optional.empty();
        }

        String userId = userIdObj.toString();
        return userRepository.findById(userId)
                .map(user -> Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getFirstName() + " " + user.getLastName(),
                        "profile_picture", user.getProfilePictureURL()
                ));
    }
}