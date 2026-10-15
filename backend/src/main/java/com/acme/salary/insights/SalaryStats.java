package com.acme.salary.insights;

import com.acme.salary.country.CurrencyConverter;
import com.acme.salary.insights.InsightsRepository.GroupStats;
import java.math.BigDecimal;

/** Salary statistics for a group, in whole units of one currency. */
public record SalaryStats(long min, long max, long average, long median, long total) {

    static SalaryStats from(GroupStats stats) {
        return new SalaryStats(Math.round(stats.min()), Math.round(stats.max()), Math.round(stats.average()),
                Math.round(stats.median()), Math.round(stats.total()));
    }

    /** Converts every figure to USD. Valid because min, max, average, median and total all scale linearly. */
    SalaryStats inUsd(BigDecimal usdRate) {
        return new SalaryStats(
                CurrencyConverter.toUsd(min, usdRate),
                CurrencyConverter.toUsd(max, usdRate),
                CurrencyConverter.toUsd(average, usdRate),
                CurrencyConverter.toUsd(median, usdRate),
                CurrencyConverter.toUsd(total, usdRate));
    }
}
