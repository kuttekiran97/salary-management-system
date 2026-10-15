package com.acme.salary.employee;

import com.acme.salary.country.Country;
import com.acme.salary.country.CurrencyConverter;
import java.time.LocalDate;

/** An employee as returned by the API, with the salary in local currency and in USD. */
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String countryCode,
        String countryName,
        String currencyCode,
        long annualSalary,
        long annualSalaryUsd,
        LocalDate joiningDate) {

    static EmployeeResponse from(Employee employee) {
        Country country = employee.getCountry();
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getJobTitle(),
                employee.getDepartment(),
                country.getCode(),
                country.getName(),
                country.getCurrencyCode(),
                employee.getAnnualSalary(),
                CurrencyConverter.toUsd(employee.getAnnualSalary(), country.getUsdRate()),
                employee.getJoiningDate());
    }
}
