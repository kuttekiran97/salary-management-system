package com.acme.salary.insights;

import com.acme.salary.common.FieldException;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;
import com.acme.salary.employee.JobCatalog;
import com.acme.salary.insights.InsightsRepository.Amount;
import com.acme.salary.insights.InsightsRepository.GroupStats;
import com.acme.salary.insights.InsightsRepository.Grouping;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Answers "how does the organisation pay people?" by country, job title and overall. */
@Service
public class InsightsService {

    private static final SalaryStats NO_SALARIES = new SalaryStats(0, 0, 0, 0, 0);

    private final InsightsRepository insights;
    private final CountryRepository countries;

    public InsightsService(InsightsRepository insights, CountryRepository countries) {
        this.insights = insights;
        this.countries = countries;
    }

    /** Organisation-wide figures. Everything is in USD, since employees are paid in several currencies. */
    public Overview overview() {
        List<GroupStats> all = insights.salaryStats(Grouping.ALL, Amount.USD, null);
        if (all.isEmpty()) {
            return new Overview(0, NO_SALARIES);
        }
        return new Overview(all.getFirst().headcount(), SalaryStats.from(all.getFirst()));
    }

    /** Per country, in the local currency and converted to USD for comparison. */
    public List<CountryInsight> byCountry() {
        Map<String, Country> countriesByCode = countries.findAll().stream()
                .collect(Collectors.toMap(Country::getCode, Function.identity()));

        return insights.salaryStats(Grouping.COUNTRY, Amount.LOCAL, null).stream()
                .map(stats -> {
                    Country country = countriesByCode.get(stats.key());
                    SalaryStats local = SalaryStats.from(stats);
                    return new CountryInsight(country.getCode(), country.getName(), country.getCurrencyCode(),
                            stats.headcount(), local, local.inUsd(country.getUsdRate()));
                })
                .toList();
    }

    /**
     * Per job title. Within one country amounts are in its local currency;
     * across all countries they are in USD.
     *
     * @param countryCode a country to limit to, or null/blank for all countries
     */
    public JobTitleInsights byJobTitle(String countryCode) {
        boolean allCountries = countryCode == null || countryCode.isBlank();
        String currency = "USD";
        String code = null;
        if (!allCountries) {
            code = countryCode.trim().toUpperCase(Locale.ROOT);
            currency = countries.findById(code)
                    .orElseThrow(() -> FieldException.invalid("country", "Unknown country: " + countryCode))
                    .getCurrencyCode();
        }

        List<JobTitleInsight> titles = insights
                .salaryStats(Grouping.JOB_TITLE, allCountries ? Amount.USD : Amount.LOCAL, code).stream()
                .map(stats -> new JobTitleInsight(stats.key(), JobCatalog.departmentOf(stats.key()),
                        stats.headcount(), SalaryStats.from(stats)))
                .toList();
        return new JobTitleInsights(code, currency, titles);
    }

    public record Overview(long headcount, SalaryStats salaryUsd) {
    }

    public record CountryInsight(String countryCode, String countryName, String currencyCode, long headcount,
                                 SalaryStats salary, SalaryStats salaryUsd) {
    }

    public record JobTitleInsights(String countryCode, String currencyCode, List<JobTitleInsight> jobTitles) {
    }

    public record JobTitleInsight(String jobTitle, String department, long headcount, SalaryStats salary) {
    }
}
