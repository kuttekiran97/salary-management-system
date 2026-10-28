package com.acme.salary.employee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.common.FieldException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.common.PageResponse;
import java.time.LocalDate;
import java.util.List;
import com.acme.salary.auth.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** HTTP contract: routes, status codes, validation and error format. The service is mocked. */
@Import(SecurityConfig.class)
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    static final EmployeeResponse PRIYA = new EmployeeResponse(1L, "EMP-00001", "Priya Sharma", "priya@acme.com",
            "Software Engineer", "Engineering", "IN", "India", "INR", 1_800_000, 21_600, LocalDate.of(2022, 4, 1));

    static final String VALID_BODY = """
            {"employeeCode": "EMP-00001", "fullName": "Priya Sharma", "email": "priya@acme.com",
             "jobTitle": "Software Engineer", "department": "Engineering", "countryCode": "IN",
             "annualSalary": 1800000, "joiningDate": "2022-04-01"}
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    EmployeeService service;

    @Test
    void listPassesFiltersToServiceAndReturnsAPage() throws Exception {
        EmployeeQuery expected = new EmployeeQuery("priya", "IN", null, null, EmployeeSort.SALARY, true);
        when(service.search(expected, 2, 50)).thenReturn(new PageResponse<>(List.of(PRIYA), 2, 50, 101, 3));

        mvc.perform(get("/api/employees?search=priya&country=IN&sort=SALARY&descending=true&page=2&size=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].fullName").value("Priya Sharma"))
                .andExpect(jsonPath("$.items[0].annualSalaryUsd").value(21_600))
                .andExpect(jsonPath("$.items[0].joiningDate").value("2022-04-01"))
                .andExpect(jsonPath("$.totalItems").value(101))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void listRejectsPageSizeAbove100() throws Exception {
        mvc.perform(get("/api/employees?size=500")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void listRejectsUnknownSortField() throws Exception {
        mvc.perform(get("/api/employees?sort=SHOE_SIZE")).andExpect(status().isBadRequest());
    }

    @Test
    void getReturns404ForMissingEmployee() throws Exception {
        when(service.get(99L)).thenThrow(new NotFoundException("Employee 99 not found"));

        mvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee 99 not found"));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(PRIYA);

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createReportsEachInvalidField() throws Exception {
        String invalid = """
                {"employeeCode": "123", "fullName": "", "email": "not-an-email",
                 "jobTitle": "Software Engineer", "department": "Engineering", "countryCode": "IN",
                 "annualSalary": -5, "joiningDate": "2999-01-01"}
                """;

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeCode").value("must look like EMP-00001"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.annualSalary").exists())
                .andExpect(jsonPath("$.errors.joiningDate").exists());
        verifyNoInteractions(service);
    }

    @Test
    void createReturns409ForDuplicateEmail() throws Exception {
        when(service.create(any())).thenThrow(FieldException.conflict("email", "Another employee already has this email"));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("Another employee already has this email"));
    }

    @Test
    void createReturns400ForBusinessRuleViolation() throws Exception {
        when(service.create(any())).thenThrow(FieldException.invalid("countryCode", "Unknown country: XX"));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.countryCode").value("Unknown country: XX"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/employees/1")).andExpect(status().isNoContent());
        verify(service).delete(eq(1L));
    }

    @Test
    void deleteReturns404ForMissingEmployee() throws Exception {
        doThrow(new NotFoundException("Employee 99 not found")).when(service).delete(99L);

        mvc.perform(delete("/api/employees/99")).andExpect(status().isNotFound());
    }
}
