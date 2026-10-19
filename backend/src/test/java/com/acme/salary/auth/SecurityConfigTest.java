package com.acme.salary.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Sign-in rules: every API call needs the HR Manager's credentials (hr / test-password in tests). */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=https://salaries.example.com")
class SecurityConfigTest {

    @Autowired
    MockMvc mvc;

    @Test
    void apiWithoutCredentialsIs401ProblemWithoutBrowserPopup() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Sign in with the HR Manager account"))
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        mvc.perform(get("/api/auth/me").with(httpBasic("hr", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validCredentialsReturnTheUser() throws Exception {
        mvc.perform(get("/api/auth/me").with(httpBasic("hr", "test-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("hr"));
    }

    @Test
    void noSessionCookieIsIssued() throws Exception {
        mvc.perform(get("/api/auth/me").with(httpBasic("hr", "test-password")))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void allowsTheConfiguredFrontendOrigin() throws Exception {
        mvc.perform(options("/api/employees")
                        .header("Origin", "https://salaries.example.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://salaries.example.com"));
    }

    @Test
    void rejectsOtherOrigins() throws Exception {
        mvc.perform(options("/api/employees")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
