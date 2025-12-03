package com.assistent.draftly.config.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class EncryptionConfig {

    @Value("${security.encryption.secret-key}")
    private String secretKeyBase64;

    @Bean
    public BytesEncryptor bytesEncryptor() {
        if (secretKeyBase64 == null || secretKeyBase64.isBlank()) {
            throw new IllegalStateException("Env variable not set: TOKEN_ENCRYPTION_KEY");
        }
        byte[] key = Base64.getDecoder().decode(secretKeyBase64);

        // Convert raw AES key bytes → SecretKey
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        return new AesBytesEncryptor(
                secretKey,
                KeyGenerators.secureRandom(),  // Proper IV generation
                AesBytesEncryptor.CipherAlgorithm.GCM            // Strong cipher mode
        );
    }
}