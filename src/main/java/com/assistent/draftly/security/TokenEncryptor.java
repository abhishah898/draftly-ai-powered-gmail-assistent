package com.assistent.draftly.security;

import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class TokenEncryptor {
    private final BytesEncryptor encryptor;

    public TokenEncryptor(BytesEncryptor bytesEncryptor) {
        this.encryptor = bytesEncryptor;
    }

    public String encryptToken(String token) {
        byte[] encryptedBytes = encryptor.encrypt(token.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decryptToken(String encryptedToken) {
        byte[] decryptedBytes = encryptor.decrypt(Base64.getDecoder().decode(encryptedToken));
        return new String(decryptedBytes);
    }
}