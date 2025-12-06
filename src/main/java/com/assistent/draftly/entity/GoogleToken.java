package com.assistent.draftly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "google_token")
public class GoogleToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @NotNull
    @Column(name = "google_user_id", unique = true)
    private String googleUserId;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @Size(max = 2000)
    @Column(name = "refresh_token")
    private String refreshToken;

    @Size(max = 2000)
    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "access_token_expiry_at")
    private Instant accessTokenExpiryAt;

    @OneToOne
    @JoinColumn(name = "google_user_id", referencedColumnName = "google_user_id", insertable = false, updatable = false)
    private User user;
}