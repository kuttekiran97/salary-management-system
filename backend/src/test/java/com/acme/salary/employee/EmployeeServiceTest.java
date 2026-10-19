package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.common.FieldException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Business rules, tested without Spring or a database. */
class EmployeeServiceTest {

    static final Country INDIA = new Country("IN", "India", "INR", new BigDecimal("0.012"), LocalDate.of(2026, 10, 19));

    EmployeeRepository employees = mock(EmployeeRepository.class);
    CountryRepository countries = mock(CountryRepository.class);
    EmployeeService service = new EmployeeService(employees, countries);

    @BeforeEach
    void setUp() {
        when(countries.findById("IN")).thenReturn(Optional.of(INDIA));
        when(employees.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createsEmployeeWithSalaryInLocalCurrencyAndUsd() {
        EmployeeResponse created = service.create(request("EMP-00001", "priya@acme.com", "IN"));

        assertThat(created.fullName()).isEqualTo("Priya Sharma");
        assertThat(created.currencyCode()).isEqualTo("INR");
        assertThat(created.annualSalary()).isEqualTo(1_800_000);
        assertThat(created.annualSalaryUsd()).isEqualTo(21_600);
    }

    @Test
    void normalizesInputBeforeSaving() {
        EmployeeResponse created = service.create(new EmployeeRequest(" EMP-00001 ", "  Priya Sharma ",
                " Priya@ACME.com ", "Software Engineer", "Engineering", "in", 1_800_000L, LocalDate.of(2022, 4, 1)));

        assertThat(created.employeeCode()).isEqualTo("EMP-00001");
        assertThat(created.fullName()).isEqualTo("Priya Sharma");
        assertThat(created.email()).isEqualTo("priya@acme.com");
        assertThat(created.countryCode()).isEqualTo("IN");
    }

    @Test
    void rejectsUnknownCountry() {
        when(countries.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("EMP-00001", "priya@acme.com", "XX")))
                .isInstanceOfSatisfying(FieldException.class, e -> {
                    assertThat(e.getKind()).isEqualTo(FieldException.Kind.INVALID);
                    assertThat(e.getField()).isEqualTo("countryCode");
                });
    }

    @Test
    void rejectsJobTitleOutsideTheDepartment() {
        EmployeeRequest accountantInEngineering = new EmployeeRequest("EMP-00001", "Priya Sharma", "priya@acme.com",
                "Accountant", "Engineering", "IN", 1_800_000L, LocalDate.of(2022, 4, 1));

        assertThatThrownBy(() -> service.create(accountantInEngineering))
                .isInstanceOfSatisfying(FieldException.class, e -> assertThat(e.getField()).isEqualTo("jobTitle"));
        verify(employees, never()).save(any());
    }

    @Test
    void rejectsDuplicateEmployeeCode() {
        when(employees.existsByEmployeeCode("EMP-00001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP-00001", "priya@acme.com", "IN")))
                .isInstanceOfSatisfying(FieldException.class, e -> {
                    assertThat(e.getKind()).isEqualTo(FieldException.Kind.CONFLICT);
                    assertThat(e.getField()).isEqualTo("employeeCode");
                });
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        when(employees.existsByEmail("priya@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP-00001", "PRIYA@acme.com", "IN")))
                .isInstanceOfSatisfying(FieldException.class, e -> assertThat(e.getField()).isEqualTo("email"));
    }

    @Test
    void updatesSalaryOfExistingEmployee() {
        Employee existing = new Employee("EMP-00001", "Priya Sharma", "priya@acme.com", "Software Engineer",
                "Engineering", INDIA, 1_800_000, LocalDate.of(2022, 4, 1));
        when(employees.findById(1L)).thenReturn(Optional.of(existing));

        EmployeeRequest raise = new EmployeeRequest("EMP-00001", "Priya Sharma", "priya@acme.com",
                "Senior Software Engineer", "Engineering", "IN", 2_400_000L, LocalDate.of(2022, 4, 1));
        EmployeeResponse updated = service.update(1L, raise);

        assertThat(updated.jobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(updated.annualSalary()).isEqualTo(2_400_000);
    }

    @Test
    void updateRejectsEmailUsedByAnotherEmployee() {
        when(employees.findById(1L)).thenReturn(Optional.of(new Employee("EMP-00001", "Priya Sharma",
                "priya@acme.com", "Software Engineer", "Engineering", INDIA, 1, LocalDate.of(2022, 4, 1))));
        when(employees.existsByEmailAndIdNot("arjun@acme.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("EMP-00001", "arjun@acme.com", "IN")))
                .isInstanceOfSatisfying(FieldException.class, e -> assertThat(e.getField()).isEqualTo("email"));
    }

    @Test
    void updateOrDeleteOfMissingEmployeeIsNotFound() {
        when(employees.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("EMP-00001", "priya@acme.com", "IN")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
    }

    private static EmployeeRequest request(String code, String email, String country) {
        return new EmployeeRequest(code, "Priya Sharma", email, "Software Engineer", "Engineering",
                country, 1_800_000L, LocalDate.of(2022, 4, 1));
    }
}
