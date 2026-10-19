package com.acme.salary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Verifies the Flyway migrations create a schema that rejects invalid data. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchemaMigrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void seedsReferenceCountries() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM country", Integer.class);
        String currency = jdbc.queryForObject("SELECT currency_code FROM country WHERE code = 'IN'", String.class);

        assertThat(count).isEqualTo(7);
        assertThat(currency).isEqualTo("INR");
    }

    @Test
    void everyCountryHasADatedRate() {
        Integer undated = jdbc.queryForObject(
                "SELECT COUNT(*) FROM country WHERE rate_as_of <> '2026-10-19' OR usd_rate <= 0", Integer.class);
        String indiaRate = jdbc.queryForObject("SELECT usd_rate FROM country WHERE code = 'IN'", String.class);

        assertThat(undated).isZero();
        assertThat(indiaRate).isEqualTo("0.010534");
    }

    @Test
    void acceptsAValidEmployee() {
        insertEmployee("EMP-00001", "priya@acme.com", "IN", 1_800_000);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class)).isEqualTo(1);
    }

    @Test
    void rejectsNonPositiveSalary() {
        assertThatThrownBy(() -> insertEmployee("EMP-00001", "priya@acme.com", "IN", 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsUnknownCountry() {
        assertThatThrownBy(() -> insertEmployee("EMP-00001", "priya@acme.com", "XX", 1_000))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateEmployeeCode() {
        insertEmployee("EMP-00001", "priya@acme.com", "IN", 1_000);

        assertThatThrownBy(() -> insertEmployee("EMP-00001", "other@acme.com", "IN", 1_000))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateEmail() {
        insertEmployee("EMP-00001", "priya@acme.com", "IN", 1_000);

        assertThatThrownBy(() -> insertEmployee("EMP-00002", "priya@acme.com", "IN", 1_000))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertEmployee(String code, String email, String country, long salary) {
        jdbc.update("""
                INSERT INTO employee (employee_code, full_name, email, job_title, department,
                                      country_code, annual_salary, joining_date, created_at, updated_at)
                VALUES (?, 'Priya Sharma', ?, 'Software Engineer', 'Engineering', ?, ?, '2022-04-01',
                        '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
                """, code, email, country, salary);
    }
}
