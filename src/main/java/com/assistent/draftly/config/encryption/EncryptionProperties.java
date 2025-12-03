package com.assistent.draftly.config.encryption;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.encryption")
public class EncryptionProperties {
    private String secretKey;
    private String salt;
}