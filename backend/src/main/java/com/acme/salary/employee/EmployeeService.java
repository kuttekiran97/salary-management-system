package com.acme.salary.employee;

import com.acme.salary.common.FieldException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.common.PageResponse;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business rules for managing employees and their salaries. */
@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employees;
    private final CountryRepository countries;

    public EmployeeService(EmployeeRepository employees, CountryRepository countries) {
        this.employees = employees;
        this.countries = countries;
    }

    public PageResponse<EmployeeResponse> search(EmployeeQuery query, int page, int size) {
        return PageResponse.from(employees
                .findAll(EmployeeSpecifications.matching(query), PageRequest.of(page, size))
                .map(EmployeeResponse::from));
    }

    public EmployeeResponse get(long id) {
        return EmployeeResponse.from(find(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Normalized input = normalize(request);
        if (employees.existsByEmployeeCode(input.employeeCode())) {
            throw duplicateCode();
        }
        if (employees.existsByEmail(input.email())) {
            throw duplicateEmail();
        }
        Employee employee = new Employee(input.employeeCode(), input.fullName(), input.email(), input.jobTitle(),
                input.department(), countryFor(input), input.annualSalary(), input.joiningDate());
        return EmployeeResponse.from(employees.save(employee));
    }

    @Transactional
    public EmployeeResponse update(long id, EmployeeRequest request) {
        Employee employee = find(id);
        Normalized input = normalize(request);
        if (employees.existsByEmployeeCodeAndIdNot(input.employeeCode(), id)) {
            throw duplicateCode();
        }
        if (employees.existsByEmailAndIdNot(input.email(), id)) {
            throw duplicateEmail();
        }
        employee.update(input.employeeCode(), input.fullName(), input.email(), input.jobTitle(),
                input.department(), countryFor(input), input.annualSalary(), input.joiningDate());
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void delete(long id) {
        employees.delete(find(id));
    }

    private Employee find(long id) {
        return employees.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee " + id + " not found"));
    }

    private Country countryFor(Normalized input) {
        return countries.findById(input.countryCode())
                .orElseThrow(() -> FieldException.invalid("countryCode", "Unknown country: " + input.countryCode()));
    }

    /** Trims text, lower-cases the email and upper-cases the country, then checks the job catalog. */
    private static Normalized normalize(EmployeeRequest request) {
        Normalized input = new Normalized(
                request.employeeCode().trim(),
                request.fullName().trim(),
                request.email().trim().toLowerCase(Locale.ROOT),
                request.jobTitle().trim(),
                request.department().trim(),
                request.countryCode().trim().toUpperCase(Locale.ROOT),
                request.annualSalary(),
                request.joiningDate());
        if (!JobCatalog.isValid(input.department(), input.jobTitle())) {
            throw FieldException.invalid("jobTitle",
                    "'" + input.jobTitle() + "' is not a job title in " + input.department());
        }
        return input;
    }

    private static FieldException duplicateCode() {
        return FieldException.conflict("employeeCode", "Another employee already has this employee code");
    }

    private static FieldException duplicateEmail() {
        return FieldException.conflict("email", "Another employee already has this email");
    }

    private record Normalized(String employeeCode, String fullName, String email, String jobTitle,
                              String department, String countryCode, long annualSalary,
                              LocalDate joiningDate) {
    }
}
