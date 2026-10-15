package com.acme.salary.country;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/** Reference data: a country ACME employs people in, with its currency and fixed USD rate. */
@Entity
public class Country {

    @Id
    private String code;

    private String name;

    private String currencyCode;

    @Column(name = "usd_rate")
    private BigDecimal usdRate;

    protected Country() {
    }

    public Country(String code, String name, String currencyCode, BigDecimal usdRate) {
        this.code = code;
        this.name = name;
        this.currencyCode = currencyCode;
        this.usdRate = usdRate;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getUsdRate() {
        return usdRate;
    }
}
