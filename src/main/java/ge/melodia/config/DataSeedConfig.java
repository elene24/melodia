/*
 * DataSeedConfig.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Runs the demo data seeding at startup.
 */
package ge.melodia.config;

import ge.melodia.service.SeedDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Inserts demo data when the application starts, unless
 * {@code app.seed.enabled=false}.
 */
@Configuration
public class DataSeedConfig {

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
    public CommandLineRunner seedDatabase(SeedDataService seedDataService) {
        return args -> seedDataService.seedIfEmpty();
    }
}
