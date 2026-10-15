package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JobCatalogTest {

    @Test
    void acceptsTitleFromItsDepartment() {
        assertThat(JobCatalog.isValid("Engineering", "Software Engineer")).isTrue();
    }

    @Test
    void rejectsTitleFromAnotherDepartment() {
        assertThat(JobCatalog.isValid("Finance", "Software Engineer")).isFalse();
    }

    @Test
    void rejectsUnknownDepartmentOrTitle() {
        assertThat(JobCatalog.isValid("Space Travel", "Astronaut")).isFalse();
        assertThat(JobCatalog.isValid("Engineering", "Sr. Engineer")).isFalse();
    }

    @Test
    void everyDepartmentHasAtLeastOneTitle() {
        assertThat(JobCatalog.titlesByDepartment()).allSatisfy((department, titles) -> assertThat(titles).isNotEmpty());
    }
}
