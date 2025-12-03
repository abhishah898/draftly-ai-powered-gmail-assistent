package com.assistent.draftly.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class TokenEncryptor {
    @Value("${security.encryption.secret-key}")
    private static String SECRET_KEY;

    @Value("${security.encryption.secret-key}")
    private static String SALT;

    private static final TextEncryptor textEncryptor = Encryptors.text(SECRET_KEY, SALT);

    public static String encryptToken(String token) {
        return textEncryptor.encrypt(token);
    }

    public static String decryptToken(String encryptedToken) {
        return textEncryptor.decrypt(encryptedToken);
    }
}
