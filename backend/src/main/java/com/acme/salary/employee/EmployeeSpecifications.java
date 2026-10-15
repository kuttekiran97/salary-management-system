package com.acme.salary.employee;

import com.acme.salary.country.Country;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Builds the database query for an {@link EmployeeQuery}. */
final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeQuery query) {
        return (root, criteria, cb) -> {
            Join<Employee, Country> country = root.join("country");

            // Ordering is set here rather than through Pageable because sorting by salary
            // needs a computed expression (salary x USD rate). Count queries skip it.
            if (criteria.getResultType() != Long.class) {
                criteria.orderBy(orderFor(query, root, country, cb));
            }
            return cb.and(filtersFor(query, root, country, cb).toArray(Predicate[]::new));
        };
    }

    private static List<Predicate> filtersFor(EmployeeQuery query, Root<Employee> root,
                                              Join<Employee, Country> country, CriteriaBuilder cb) {
        List<Predicate> filters = new ArrayList<>();
        if (hasText(query.search())) {
            String pattern = "%" + query.search().trim().toLowerCase(Locale.ROOT) + "%";
            filters.add(cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("employeeCode")), pattern)));
        }
        if (hasText(query.countryCode())) {
            filters.add(cb.equal(country.get("code"), query.countryCode()));
        }
        if (hasText(query.department())) {
            filters.add(cb.equal(root.get("department"), query.department()));
        }
        if (hasText(query.jobTitle())) {
            filters.add(cb.equal(root.get("jobTitle"), query.jobTitle()));
        }
        return filters;
    }

    private static List<Order> orderFor(EmployeeQuery query, Root<Employee> root,
                                        Join<Employee, Country> country, CriteriaBuilder cb) {
        Expression<?> key = switch (query.sort() == null ? EmployeeSort.NAME : query.sort()) {
            case NAME -> root.get("fullName");
            case EMPLOYEE_CODE -> root.get("employeeCode");
            case JOINING_DATE -> root.get("joiningDate");
            case SALARY -> cb.prod(root.<Number>get("annualSalary"), country.<Number>get("usdRate"));
        };
        Order primary = query.descending() ? cb.desc(key) : cb.asc(key);
        // Tie-breaker keeps page boundaries stable when sort keys are equal
        return List.of(primary, cb.asc(root.get("id")));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
