package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import com.acme.salary.insights.InsightsRepository.Amount;
import com.acme.salary.insights.InsightsRepository.GroupStats;
import com.acme.salary.insights.InsightsRepository.Grouping;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** Runs the statistics SQL against the real schema (in-memory SQLite). */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InsightsRepository.class)
class InsightsRepositoryTest {

    @Autowired
    InsightsRepository insights;

    @Autowired
    JdbcTemplate jdbc;

    private int nextId = 1;

    @BeforeEach
    void setUp() {
        // India (INR, rate 0.010534 from V3): 3 employees -> odd count, median is the middle value
        insert("IN", "Software Engineer", 1_000_000);
        insert("IN", "Software Engineer", 2_000_000);
        insert("IN", "Accountant", 6_000_000);
        // US (USD): 4 employees -> even count, median averages the two middle values
        insert("US", "Software Engineer", 100_000);
        insert("US", "Software Engineer", 120_000);
        insert("US", "Accountant", 80_000);
        insert("US", "Accountant", 200_000);
    }

    @Test
    void medianOfOddGroupIsTheMiddleValue() {
        GroupStats india = find(insights.salaryStats(Grouping.COUNTRY, Amount.LOCAL, null), "IN");

        assertThat(india.headcount()).isEqualTo(3);
        assertThat(india.median()).isEqualTo(2_000_000);
    }

    @Test
    void medianOfEvenGroupAveragesTheTwoMiddleValues() {
        GroupStats us = find(insights.salaryStats(Grouping.COUNTRY, Amount.LOCAL, null), "US");

        assertThat(us.median()).isEqualTo(110_000); // (100,000 + 120,000) / 2
    }

    @Test
    void computesMinMaxAverageAndTotalPerGroup() {
        GroupStats us = find(insights.salaryStats(Grouping.COUNTRY, Amount.LOCAL, null), "US");

        assertThat(us.min()).isEqualTo(80_000);
        assertThat(us.max()).isEqualTo(200_000);
        assertThat(us.average()).isEqualTo(125_000);
        assertThat(us.total()).isEqualTo(500_000);
    }

    @Test
    void groupsByJobTitleAcrossCountriesInUsd() {
        // Software Engineers in USD: 10,534 | 21,068 | 100,000 | 120,000
        GroupStats engineers = find(insights.salaryStats(Grouping.JOB_TITLE, Amount.USD, null), "Software Engineer");

        assertThat(engineers.headcount()).isEqualTo(4);
        assertThat(engineers.min()).isEqualTo(10_534, offset(0.01));
        assertThat(engineers.median()).isEqualTo(60_534, offset(0.01));
    }

    @Test
    void limitsStatisticsToOneCountry() {
        List<GroupStats> indiaByTitle = insights.salaryStats(Grouping.JOB_TITLE, Amount.LOCAL, "IN");

        assertThat(indiaByTitle).extracting(GroupStats::key).containsExactly("Software Engineer", "Accountant");
        assertThat(find(indiaByTitle, "Software Engineer").median()).isEqualTo(1_500_000);
    }

    @Test
    void summarisesEveryoneAsOneGroup() {
        List<GroupStats> all = insights.salaryStats(Grouping.ALL, Amount.USD, null);

        assertThat(all).hasSize(1);
        assertThat(all.getFirst().headcount()).isEqualTo(7);
    }

    @Test
    void returnsNoGroupsWhenThereAreNoEmployees() {
        jdbc.update("DELETE FROM employee");

        assertThat(insights.salaryStats(Grouping.COUNTRY, Amount.LOCAL, null)).isEmpty();
    }

    private static GroupStats find(List<GroupStats> groups, String key) {
        return groups.stream().filter(g -> g.key().equals(key)).findFirst().orElseThrow();
    }

    private void insert(String country, String jobTitle, long salary) {
        int id = nextId++;
        jdbc.update("""
                INSERT INTO employee (employee_code, full_name, email, job_title, department, country_code,
                                      annual_salary, joining_date, created_at, updated_at)
                VALUES (?, 'Test Person', ?, ?, 'Any', ?, ?, '2022-04-01', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
                """, "EMP-%05d".formatted(id), "person" + id + "@acme.com", jobTitle, country, salary);
    }
}
