package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.country.CountryRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/** Runs the search, filter and sort query against the real schema (in-memory SQLite). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeSearchTest {

    @Autowired
    EmployeeRepository employees;

    @Autowired
    CountryRepository countries;

    @BeforeEach
    void setUp() {
        // USD equivalents: Priya 21,600 | Arjun 36,000 | John 150,000 | Emma 101,600
        save("EMP-00001", "Priya Sharma", "Software Engineer", "Engineering", "IN", 1_800_000);
        save("EMP-00002", "Arjun Mehta", "Data Scientist", "Data", "IN", 3_000_000);
        save("EMP-00003", "John Smith", "Software Engineer", "Engineering", "US", 150_000);
        save("EMP-00004", "Emma Brown", "Accountant", "Finance", "GB", 80_000);
    }

    @Test
    void returnsEveryoneWhenNoFiltersAreGiven() {
        assertThat(names(query(null, null, null, null))).hasSize(4);
    }

    @Test
    void searchMatchesPartOfNameIgnoringCase() {
        assertThat(names(query("SHARMA", null, null, null))).containsExactly("Priya Sharma");
    }

    @Test
    void searchMatchesEmployeeCodeAndEmail() {
        assertThat(names(query("emp-00003", null, null, null))).containsExactly("John Smith");
        assertThat(names(query("emma.brown@", null, null, null))).containsExactly("Emma Brown");
    }

    @Test
    void filtersCombine() {
        assertThat(names(query(null, "IN", "Engineering", null))).containsExactly("Priya Sharma");
        assertThat(names(query(null, null, null, "Software Engineer"))).containsExactly("John Smith", "Priya Sharma");
    }

    @Test
    void sortsByNameByDefault() {
        assertThat(names(query(null, null, null, null)))
                .containsExactly("Arjun Mehta", "Emma Brown", "John Smith", "Priya Sharma");
    }

    @Test
    void sortsSalaryByUsdEquivalentNotLocalAmount() {
        // By local amount, Arjun (3,000,000 INR) would come first.
        EmployeeQuery highestPaidFirst = new EmployeeQuery(null, null, null, null, EmployeeSort.SALARY, true);

        assertThat(names(employees.findAll(EmployeeSpecifications.matching(highestPaidFirst), PageRequest.of(0, 10))))
                .containsExactly("John Smith", "Emma Brown", "Arjun Mehta", "Priya Sharma");
    }

    @Test
    void paginatesResults() {
        Page<Employee> secondPage = employees.findAll(
                EmployeeSpecifications.matching(new EmployeeQuery(null, null, null, null, EmployeeSort.NAME, false)),
                PageRequest.of(1, 3));

        assertThat(secondPage.getTotalElements()).isEqualTo(4);
        assertThat(secondPage.getTotalPages()).isEqualTo(2);
        assertThat(names(secondPage)).containsExactly("Priya Sharma");
    }

    private Page<Employee> query(String search, String country, String department, String jobTitle) {
        EmployeeQuery query = new EmployeeQuery(search, country, department, jobTitle, null, false);
        return employees.findAll(EmployeeSpecifications.matching(query), PageRequest.of(0, 10));
    }

    private static List<String> names(Page<Employee> page) {
        return page.map(Employee::getFullName).getContent();
    }

    private void save(String code, String name, String title, String department, String country, long salary) {
        String email = name.toLowerCase().replace(' ', '.') + "@acme.com";
        employees.save(new Employee(code, name, email, title, department,
                countries.getReferenceById(country), salary, LocalDate.of(2022, 4, 1)));
    }
}
