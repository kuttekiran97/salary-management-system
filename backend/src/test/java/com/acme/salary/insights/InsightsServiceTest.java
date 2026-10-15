package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.acme.salary.common.FieldException;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;
import com.acme.salary.insights.InsightsRepository.Amount;
import com.acme.salary.insights.InsightsRepository.GroupStats;
import com.acme.salary.insights.InsightsRepository.Grouping;
import com.acme.salary.insights.InsightsService.CountryInsight;
import com.acme.salary.insights.InsightsService.JobTitleInsights;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Rounding, currency conversion and labelling. The SQL itself is covered by InsightsRepositoryTest. */
class InsightsServiceTest {

    static final Country INDIA = new Country("IN", "India", "INR", new BigDecimal("0.012"));

    InsightsRepository repository = mock(InsightsRepository.class);
    CountryRepository countries = mock(CountryRepository.class);
    InsightsService service = new InsightsService(repository, countries);

    @Test
    void overviewIsZeroWhenThereAreNoEmployees() {
        when(repository.salaryStats(Grouping.ALL, Amount.USD, null)).thenReturn(List.of());

        assertThat(service.overview().headcount()).isZero();
        assertThat(service.overview().salaryUsd().total()).isZero();
    }

    @Test
    void overviewRoundsToWholeDollars() {
        when(repository.salaryStats(Grouping.ALL, Amount.USD, null))
                .thenReturn(List.of(new GroupStats("ALL", 3, 999.4, 1000.6, 1000.5, 1000.49, 3001.5)));

        SalaryStats usd = service.overview().salaryUsd();

        assertThat(usd).isEqualTo(new SalaryStats(999, 1001, 1001, 1000, 3002));
    }

    @Test
    void byCountryReportsLocalCurrencyAndUsd() {
        when(countries.findAll()).thenReturn(List.of(INDIA));
        when(repository.salaryStats(Grouping.COUNTRY, Amount.LOCAL, null)).thenReturn(List.of(
                new GroupStats("IN", 2, 1_000_000, 2_000_000, 1_500_000, 1_500_000, 3_000_000)));

        CountryInsight india = service.byCountry().getFirst();

        assertThat(india.countryName()).isEqualTo("India");
        assertThat(india.currencyCode()).isEqualTo("INR");
        assertThat(india.salary().median()).isEqualTo(1_500_000);
        assertThat(india.salaryUsd().median()).isEqualTo(18_000);
        assertThat(india.salaryUsd().total()).isEqualTo(36_000);
    }

    @Test
    void byJobTitleAcrossCountriesIsInUsd() {
        when(repository.salaryStats(Grouping.JOB_TITLE, Amount.USD, null)).thenReturn(List.of(
                new GroupStats("Data Scientist", 1, 50_000, 50_000, 50_000, 50_000, 50_000)));

        JobTitleInsights result = service.byJobTitle(null);

        assertThat(result.currencyCode()).isEqualTo("USD");
        assertThat(result.jobTitles().getFirst().department()).isEqualTo("Data");
    }

    @Test
    void byJobTitleWithinACountryIsInLocalCurrency() {
        when(countries.findById("IN")).thenReturn(Optional.of(INDIA));
        when(repository.salaryStats(Grouping.JOB_TITLE, Amount.LOCAL, "IN")).thenReturn(List.of(
                new GroupStats("Data Scientist", 1, 3_000_000, 3_000_000, 3_000_000, 3_000_000, 3_000_000)));

        JobTitleInsights result = service.byJobTitle("in");

        assertThat(result.countryCode()).isEqualTo("IN");
        assertThat(result.currencyCode()).isEqualTo("INR");
        assertThat(result.jobTitles().getFirst().salary().median()).isEqualTo(3_000_000);
    }

    @Test
    void byJobTitleRejectsUnknownCountry() {
        when(countries.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.byJobTitle("XX"))
                .isInstanceOfSatisfying(FieldException.class, e -> assertThat(e.getField()).isEqualTo("country"));
    }
}
