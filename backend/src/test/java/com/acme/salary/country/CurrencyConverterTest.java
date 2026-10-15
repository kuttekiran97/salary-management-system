package com.acme.salary.country;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CurrencyConverterTest {

    @Test
    void convertsLocalAmountToUsd() {
        assertThat(CurrencyConverter.toUsd(1_800_000, new BigDecimal("0.012"))).isEqualTo(21_600);
    }

    @Test
    void usdAmountsAreUnchanged() {
        assertThat(CurrencyConverter.toUsd(150_000, BigDecimal.ONE)).isEqualTo(150_000);
    }

    @Test
    void roundsHalfUpToWholeDollars() {
        assertThat(CurrencyConverter.toUsd(1_000, new BigDecimal("0.0125"))).isEqualTo(13); // 12.5 -> 13
        assertThat(CurrencyConverter.toUsd(1_000, new BigDecimal("0.0124"))).isEqualTo(12); // 12.4 -> 12
    }
}
