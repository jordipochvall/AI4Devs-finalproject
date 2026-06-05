package com.novacasino.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.novacasino")
@EntityScan(basePackages = "com.novacasino.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.novacasino.infrastructure.persistence.repository")
public class NovaCasinoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaCasinoApplication.class, args);
    }
}
