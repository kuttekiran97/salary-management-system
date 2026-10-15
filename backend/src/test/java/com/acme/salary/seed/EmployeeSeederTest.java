package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EmployeeSeeder.class)
class EmployeeSeederTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    @Autowired
    EmployeeSeeder seeder;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void insertsEmployeesThatSatisfyTheSchemaConstraints() {
        int inserted = seeder.seed(500, TODAY);

        assertThat(inserted).isEqualTo(500);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class)).isEqualTo(500);
    }

    @Test
    void doesNothingWhenEmployeesAlreadyExist() {
        seeder.seed(10, TODAY);

        assertThat(seeder.seed(10, TODAY)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class)).isEqualTo(10);
    }
}
