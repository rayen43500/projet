package com.fgm.gestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Point d'entrée FGM.
 * Scanne les deux packages racines :
 *   - com.fgm.gestion.*  : auth, seed, JWT, Security
 *   - org.example.backend.* : controllers, services, batch, repositories
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.fgm.gestion",
    "org.example.backend"
})
@EnableMongoRepositories(basePackages = {
    "com.fgm.gestion.repository",
    "org.example.backend.repository"
})
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
