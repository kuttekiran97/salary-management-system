package com.acme.salary.employee;

/**
 * Search, filters and sort order for the employee list. Null or blank filters are ignored.
 *
 * @param search matches part of the name, email or employee code, ignoring case
 */
public record EmployeeQuery(
        String search,
        String countryCode,
        String department,
        String jobTitle,
        EmployeeSort sort,
        boolean descending) {
}
