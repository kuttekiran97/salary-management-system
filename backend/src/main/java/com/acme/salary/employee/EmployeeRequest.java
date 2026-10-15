package com.acme.salary.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Data for creating or updating an employee. Format rules are checked here; business rules in the service. */
public record EmployeeRequest(
        @NotBlank @Pattern(regexp = "\\s*EMP-\\d{5}\\s*", message = "must look like EMP-00001")
        String employeeCode,

        @NotBlank @Size(max = 100)
        String fullName,

        @NotBlank @Email @Size(max = 254)
        String email,

        @NotBlank
        String jobTitle,

        @NotBlank
        String department,

        @NotBlank @Pattern(regexp = "\\s*[A-Za-z]{2}\\s*", message = "must be a 2-letter country code")
        String countryCode,

        @NotNull @Positive
        Long annualSalary,

        @NotNull @PastOrPresent
        LocalDate joiningDate) {
}
