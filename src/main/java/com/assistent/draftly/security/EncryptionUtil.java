package com.assistent.draftly.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public class EncryptionUtil {
    private EncryptionUtil() {
        // prevents for external initialization
    }

    public static String generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64
                .getEncoder()
                .encodeToString(secretKey.getEncoded());
    }
    public static String generateSalt() {
        byte[] salt =  new byte[16]; // 128 bit salt
        new SecureRandom().nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }
}
