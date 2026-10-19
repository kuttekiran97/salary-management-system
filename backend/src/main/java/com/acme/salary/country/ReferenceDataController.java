package com.acme.salary.country;

import com.acme.salary.employee.JobCatalog;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lists the countries, departments and job titles the UI offers in its dropdowns. */
@RestController
public class ReferenceDataController {

    private final CountryRepository countries;

    public ReferenceDataController(CountryRepository countries) {
        this.countries = countries;
    }

    @GetMapping("/api/reference-data")
    public ReferenceData referenceData() {
        List<CountryOption> countryOptions = countries.findAll(Sort.by("name")).stream()
                .map(c -> new CountryOption(c.getCode(), c.getName(), c.getCurrencyCode(), c.getUsdRate(), c.getRateAsOf()))
                .toList();
        List<DepartmentOption> departments = JobCatalog.titlesByDepartment().entrySet().stream()
                .map(entry -> new DepartmentOption(entry.getKey(), entry.getValue()))
                .toList();
        return new ReferenceData(countryOptions, departments);
    }

    public record ReferenceData(List<CountryOption> countries, List<DepartmentOption> departments) {
    }

    public record CountryOption(String code, String name, String currencyCode, BigDecimal usdRate,
                                LocalDate rateAsOf) {
    }

    public record DepartmentOption(String name, List<String> jobTitles) {
    }
}
