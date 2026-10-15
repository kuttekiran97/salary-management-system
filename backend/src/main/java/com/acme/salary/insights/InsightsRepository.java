package com.acme.salary.insights;

import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Salary statistics computed in the database, so only one row per group leaves it.
 *
 * <p>SQLite has no MEDIAN function, so salaries are ranked within each group with window
 * functions and the middle value (or the average of the two middle values) is taken.
 */
@Repository
public class InsightsRepository {

    /** What to group by. Each constant maps to a fixed SQL expression, never user input. */
    public enum Grouping {
        ALL("'ALL'"),
        COUNTRY("e.country_code"),
        JOB_TITLE("e.job_title");

        private final String sql;

        Grouping(String sql) {
            this.sql = sql;
        }
    }

    /** Which amount to aggregate. LOCAL is only meaningful when every row shares one currency. */
    public enum Amount {
        LOCAL("e.annual_salary"),
        USD("e.annual_salary * c.usd_rate");

        private final String sql;

        Amount(String sql) {
            this.sql = sql;
        }
    }

    /** Raw statistics for one group; amounts are unrounded. */
    public record GroupStats(String key, long headcount, double min, double max,
                             double average, double median, double total) {
    }

    private final JdbcTemplate jdbc;

    public InsightsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @param countryCode limit to one country, or null for all countries
     */
    public List<GroupStats> salaryStats(Grouping grouping, Amount amount, String countryCode) {
        List<Object> params = new ArrayList<>();
        String where = "";
        if (countryCode != null) {
            where = "WHERE e.country_code = ?";
            params.add(countryCode);
        }
        String sql = """
                WITH ranked AS (
                    SELECT %1$s AS group_key,
                           %2$s AS salary,
                           ROW_NUMBER() OVER (PARTITION BY %1$s ORDER BY %2$s) AS position,
                           COUNT(*)     OVER (PARTITION BY %1$s)               AS group_size
                    FROM employee e
                    JOIN country c ON c.code = e.country_code
                    %3$s
                )
                SELECT group_key,
                       COUNT(*)    AS headcount,
                       MIN(salary) AS min_salary,
                       MAX(salary) AS max_salary,
                       AVG(salary) AS avg_salary,
                       AVG(CASE WHEN position IN ((group_size + 1) / 2, (group_size + 2) / 2)
                                THEN salary END) AS median_salary,
                       SUM(salary) AS total_salary
                FROM ranked
                GROUP BY group_key
                ORDER BY headcount DESC, group_key
                """.formatted(grouping.sql, amount.sql, where);

        return jdbc.query(sql, (rs, row) -> new GroupStats(
                rs.getString("group_key"),
                rs.getLong("headcount"),
                rs.getDouble("min_salary"),
                rs.getDouble("max_salary"),
                rs.getDouble("avg_salary"),
                rs.getDouble("median_salary"),
                rs.getDouble("total_salary")), params.toArray());
    }
}
