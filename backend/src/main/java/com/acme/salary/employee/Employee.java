package com.acme.salary.employee;

import com.acme.salary.common.IsoInstantConverter;
import com.acme.salary.common.IsoLocalDateConverter;
import com.acme.salary.country.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.time.LocalDate;

/** An employee and their current annual salary, in the local currency of their country. */
@Entity
public class Employee {

    // SQLite has a single 64-bit INTEGER type; only INTEGER PRIMARY KEY auto-increments
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private Long id;

    private String employeeCode;

    private String fullName;

    private String email;

    private String jobTitle;

    private String department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_code")
    private Country country;

    @Column(columnDefinition = "integer")
    private long annualSalary;

    @Convert(converter = IsoLocalDateConverter.class)
    private LocalDate joiningDate;

    @Convert(converter = IsoInstantConverter.class)
    private Instant createdAt;

    @Convert(converter = IsoInstantConverter.class)
    private Instant updatedAt;

    protected Employee() {
    }

    public Employee(String employeeCode, String fullName, String email, String jobTitle, String department,
                    Country country, long annualSalary, LocalDate joiningDate) {
        update(employeeCode, fullName, email, jobTitle, department, country, annualSalary, joiningDate);
    }

    public void update(String employeeCode, String fullName, String email, String jobTitle, String department,
                       Country country, long annualSalary, LocalDate joiningDate) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.country = country;
        this.annualSalary = annualSalary;
        this.joiningDate = joiningDate;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public Country getCountry() {
        return country;
    }

    public long getAnnualSalary() {
        return annualSalary;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }
}
