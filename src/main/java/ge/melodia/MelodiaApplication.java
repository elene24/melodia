/*
 * MelodiaApplication.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Entry point of the Melodia music streaming application.
 */
package ge.melodia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the Spring Boot application.
 */
@SpringBootApplication
public class MelodiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MelodiaApplication.class, args);
    }
}
