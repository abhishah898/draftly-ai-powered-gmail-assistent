package com.assistent.draftly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "gmail_token")
public class GmailToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String googleUserId;

    @Column(nullable = false)
    private String email;

    @Column(length = 2000)
    private String refreshToken;

    @Column(length = 2000)
    private String accessToken;

    private Instant accessTokenExpiryAt;
}
