package com.acme.salary.country;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.acme.salary.auth.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WithMockUser(roles = "HR_MANAGER")
@WebMvcTest(ReferenceDataController.class)
class ReferenceDataControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CountryRepository countries;

    @Test
    void listsCountriesAndDepartmentsWithTheirJobTitles() throws Exception {
        when(countries.findAll(any(Sort.class)))
                .thenReturn(List.of(new Country("IN", "India", "INR", new BigDecimal("0.012"), LocalDate.of(2026, 10, 19))));

        mvc.perform(get("/api/reference-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries[0].code").value("IN"))
                .andExpect(jsonPath("$.countries[0].currencyCode").value("INR"))
                .andExpect(jsonPath("$.countries[0].rateAsOf").value("2026-10-19"))
                .andExpect(jsonPath("$.departments[0].name").value("Engineering"))
                .andExpect(jsonPath("$.departments[0].jobTitles[0]").value("Software Engineer"));
    }
}
