package com.acme.salary.auth;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lets the UI check credentials at sign-in: 200 with the user when they are valid, 401 otherwise. */
@RestController
public class AuthController {

    @GetMapping("/api/auth/me")
    public CurrentUser me(Principal principal) {
        return new CurrentUser(principal.getName());
    }

    public record CurrentUser(String username) {
    }
}
