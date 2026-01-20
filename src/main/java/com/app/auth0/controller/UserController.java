package com.app.auth0.controller;

import com.app.auth0.dto.UserRegistrationDto;
import com.app.auth0.model.User;
import com.app.auth0.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@AuthenticationPrincipal Jwt jwt, 
                                                            @RequestBody(required = false) UserRegistrationDto registrationDto) {
        String auth0Id = jwt.getSubject();
        String email = jwt.getClaimAsString("email"); // Assuming email is in the token claims
        
        // If email is not in token, try to get from body
        if (email == null && registrationDto != null) {
            email = registrationDto.getEmail();
        }

        String username = (registrationDto != null) ? registrationDto.getUsername() : null;

        User user = userService.registerUser(auth0Id, email, username);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("userId", user.getId());
        response.put("auth0Id", user.getAuth0Id());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }
}
