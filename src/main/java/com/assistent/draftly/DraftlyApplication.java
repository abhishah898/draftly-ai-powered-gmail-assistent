package com.assistent.draftly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.assistent.draftly.config")
public class DraftlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DraftlyApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║               Welcome to Draftly!              ║");
        System.out.println("║           AI-Powered Draft Generator           ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }
}