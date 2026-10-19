package com.acme.salary.country;

import com.acme.salary.common.IsoLocalDateConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Reference data: a country ACME employs people in, with its currency and a fixed, dated USD rate. */
@Entity
public class Country {

    @Id
    private String code;

    private String name;

    private String currencyCode;

    @Column(name = "usd_rate")
    private BigDecimal usdRate;

    /** The day the rate was published; fixed rates are only meaningful with their date. */
    @Convert(converter = IsoLocalDateConverter.class)
    private LocalDate rateAsOf;

    protected Country() {
    }

    public Country(String code, String name, String currencyCode, BigDecimal usdRate, LocalDate rateAsOf) {
        this.code = code;
        this.name = name;
        this.currencyCode = currencyCode;
        this.usdRate = usdRate;
        this.rateAsOf = rateAsOf;
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

    public LocalDate getRateAsOf() {
        return rateAsOf;
    }
}
