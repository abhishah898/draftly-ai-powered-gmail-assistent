package com.assistent.draftly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan("com.assistent.draftly.config")
@ComponentScan(basePackages = "com.assistent.draftly")
@EnableJpaRepositories(basePackages = "com.assistent.draftly.repository")
@EntityScan(basePackages = "com.assistent.draftly.entity")
public class DraftlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DraftlyApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║               Welcome to Draftly!              ║");
        System.out.println("║           AI-Powered Draft Generator           ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }
}