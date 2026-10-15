package com.acme.salary.seed;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/**
 * Seeds the database when the app starts with {@code app.seed.enabled=true}.
 * Safe to leave on: it does nothing once employees exist.
 */
@Component
@ConditionalOnBooleanProperty("app.seed.enabled")
class SeedOnStartup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedOnStartup.class);

    private final EmployeeSeeder seeder;
    private final int count;

    SeedOnStartup(EmployeeSeeder seeder, @Value("${app.seed.count:10000}") int count) {
        this.seeder = seeder;
        this.count = count;
    }

    @Override
    public void run(ApplicationArguments args) {
        long start = System.currentTimeMillis();
        int inserted = seeder.seed(count, LocalDate.now());
        if (inserted == 0) {
            log.info("Seeding skipped: employees already exist");
        } else {
            log.info("Seeded {} employees in {} ms", inserted, System.currentTimeMillis() - start);
        }
    }
}
