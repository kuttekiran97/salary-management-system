package com.acme.salary.country;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Converts local-currency amounts to USD using a country's fixed rate. */
public final class CurrencyConverter {

    private CurrencyConverter() {
    }

    /** Returns the amount in whole US dollars, rounded half-up. */
    public static long toUsd(long localAmount, BigDecimal usdRate) {
        return BigDecimal.valueOf(localAmount)
                .multiply(usdRate)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
