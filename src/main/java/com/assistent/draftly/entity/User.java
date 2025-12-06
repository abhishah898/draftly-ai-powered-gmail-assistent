package com.assistent.draftly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @NotNull
    @Column(unique = true, name = "google_user_id")
    private String googleUserId;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 100, name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureURL;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    GoogleToken gmailToken;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}