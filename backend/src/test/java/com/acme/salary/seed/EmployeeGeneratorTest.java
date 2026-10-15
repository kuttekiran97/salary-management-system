package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.employee.JobCatalog;
import com.acme.salary.seed.EmployeeGenerator.GeneratedEmployee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmployeeGeneratorTest {

    static final Map<String, BigDecimal> RATES = Map.of(
            "US", BigDecimal.ONE, "IN", new BigDecimal("0.012"), "GB", new BigDecimal("1.27"),
            "DE", new BigDecimal("1.08"), "CA", new BigDecimal("0.73"), "AU", new BigDecimal("0.66"),
            "SG", new BigDecimal("0.74"));
    static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    // 10,000 matches the brief and still generates in well under a second
    final List<GeneratedEmployee> employees = EmployeeGenerator.generate(10_000, RATES, TODAY, 42);

    @Test
    void generatesTheRequestedNumberWithSequentialCodes() {
        assertThat(employees).hasSize(10_000);
        assertThat(employees.getFirst().employeeCode()).isEqualTo("EMP-00001");
        assertThat(employees.getLast().employeeCode()).isEqualTo("EMP-10000");
    }

    @Test
    void sameSeedGivesTheSameEmployees() {
        assertThat(EmployeeGenerator.generate(100, RATES, TODAY, 42))
                .isEqualTo(EmployeeGenerator.generate(100, RATES, TODAY, 42));
    }

    @Test
    void emailsAreUniqueEvenWhenNamesRepeat() {
        assertThat(employees).extracting(GeneratedEmployee::email).doesNotHaveDuplicates();
    }

    @Test
    void everyJobTitleIsValidForItsDepartment() {
        assertThat(employees).allSatisfy(e -> assertThat(JobCatalog.isValid(e.department(), e.jobTitle())).isTrue());
    }

    @Test
    void usesEveryCountry() {
        assertThat(employees).extracting(GeneratedEmployee::countryCode).containsAll(RATES.keySet());
    }

    @Test
    void joiningDatesAreWithinTheLastTenYearsAndNotInTheFuture() {
        assertThat(employees).allSatisfy(e -> assertThat(e.joiningDate())
                .isBeforeOrEqualTo(TODAY)
                .isAfter(TODAY.minusYears(10).minusDays(3)));
    }

    @Test
    void salariesArePositiveWholeThousandsInLocalCurrency() {
        assertThat(employees).allSatisfy(e -> {
            assertThat(e.annualSalary()).isPositive();
            assertThat(e.annualSalary() % 1000).isZero();
        });
    }

    @Test
    void salariesAreRealisticForEachCountry() {
        // A US software engineer earns 90k-140k USD; in India the same role is paid in lakhs of rupees
        assertThat(salariesOf("US", "Software Engineer")).allSatisfy(s -> assertThat(s).isBetween(90_000L, 140_000L));
        assertThat(salariesOf("IN", "Software Engineer")).allSatisfy(s -> assertThat(s).isBetween(2_200_000L, 3_600_000L));
    }

    private List<Long> salariesOf(String country, String title) {
        return employees.stream()
                .filter(e -> e.countryCode().equals(country) && e.jobTitle().equals(title))
                .map(GeneratedEmployee::annualSalary)
                .toList();
    }
}
