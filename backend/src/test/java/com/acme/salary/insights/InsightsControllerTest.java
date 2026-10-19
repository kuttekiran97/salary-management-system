package com.acme.salary.insights;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.common.FieldException;
import com.acme.salary.insights.InsightsService.JobTitleInsight;
import com.acme.salary.insights.InsightsService.JobTitleInsights;
import com.acme.salary.insights.InsightsService.Overview;
import java.util.List;
import com.acme.salary.auth.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WithMockUser(roles = "HR_MANAGER")
@WebMvcTest(InsightsController.class)
class InsightsControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    InsightsService service;

    @Test
    void returnsOverview() throws Exception {
        when(service.overview()).thenReturn(new Overview(10_000, new SalaryStats(10, 300_000, 70_000, 60_000, 700_000_000)));

        mvc.perform(get("/api/insights/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headcount").value(10_000))
                .andExpect(jsonPath("$.salaryUsd.median").value(60_000));
    }

    @Test
    void passesCountryFilterForJobTitles() throws Exception {
        when(service.byJobTitle("IN")).thenReturn(new JobTitleInsights("IN", "INR", List.of(
                new JobTitleInsight("Data Scientist", "Data", 5, new SalaryStats(1, 2, 3, 4, 5)))));

        mvc.perform(get("/api/insights/by-job-title?country=IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyCode").value("INR"))
                .andExpect(jsonPath("$.jobTitles[0].jobTitle").value("Data Scientist"));
    }

    @Test
    void unknownCountryIsBadRequest() throws Exception {
        when(service.byJobTitle("XX")).thenThrow(FieldException.invalid("country", "Unknown country: XX"));

        mvc.perform(get("/api/insights/by-job-title?country=XX"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.country").value("Unknown country: XX"));
    }
}
