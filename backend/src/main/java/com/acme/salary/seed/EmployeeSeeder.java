package com.acme.salary.seed;

import com.acme.salary.seed.EmployeeGenerator.GeneratedEmployee;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Loads generated employees into an empty database. */
@Component
public class EmployeeSeeder {

    static final long SEED = 42;
    private static final int BATCH_SIZE = 1_000;

    private final JdbcTemplate jdbc;

    public EmployeeSeeder(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Inserts {@code count} employees, unless employees already exist.
     *
     * @return how many employees were inserted (0 if the table was not empty)
     */
    @Transactional
    public int seed(int count, LocalDate today) {
        Integer existing = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer.class);
        if (existing != null && existing > 0) {
            return 0;
        }

        List<GeneratedEmployee> employees = EmployeeGenerator.generate(count, usdRates(), today, SEED);
        String now = Instant.now().toString();
        // Plain JDBC batches: much faster than saving 10,000 JPA entities one by one
        jdbc.batchUpdate("""
                INSERT INTO employee (employee_code, full_name, email, job_title, department, country_code,
                                      annual_salary, joining_date, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, employees, BATCH_SIZE, (statement, e) -> {
            statement.setString(1, e.employeeCode());
            statement.setString(2, e.fullName());
            statement.setString(3, e.email());
            statement.setString(4, e.jobTitle());
            statement.setString(5, e.department());
            statement.setString(6, e.countryCode());
            statement.setLong(7, e.annualSalary());
            statement.setString(8, e.joiningDate().toString());
            statement.setString(9, now);
            statement.setString(10, now);
        });
        return employees.size();
    }

    private Map<String, BigDecimal> usdRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        jdbc.query("SELECT code, usd_rate FROM country",
                rs -> { rates.put(rs.getString("code"), rs.getBigDecimal("usd_rate")); });
        return rates;
    }
}
