package com.assistent.draftly.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Getter
@Setter
@Table("gmail_token")
public class GmailToken {

    @Id
    private Long id;     // AUTO_INCREMENT handled by DB schema

    @NotNull
    private String googleUserId;

    @NotNull
    @Email
    private String email;

    @Size(max = 2000)
    private String refreshToken;

    @Size(max = 2000)
    private String accessToken;

    private Instant accessTokenExpiryAt;
}