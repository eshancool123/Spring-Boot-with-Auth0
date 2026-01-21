package com.app.auth0.service;

import com.app.auth0.dto.LoginDto;
import com.app.auth0.dto.RegistrationDto;
import com.app.auth0.model.User;
import com.app.auth0.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class AuthService {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${auth0.client-id}")
    private String clientId;

    @Value("${auth0.client-secret}")
    private String clientSecret;

    @Value("${auth0.connection}")
    private String connection;

    @Value("${auth0.audience}")
    private String audience;

    private final RestTemplate restTemplate;
    private final AuthRepository authRepository;

    public AuthService(RestTemplate restTemplate, AuthRepository authRepository) {
        this.restTemplate = restTemplate;
        this.authRepository = authRepository;
    }

    public Map<String, Object> register(RegistrationDto registrationDto) {
        String url = issuerUri + "dbconnections/signup";

        Map<String, String> request = new HashMap<>();
        request.put("client_id", clientId);
        request.put("email", registrationDto.getEmail());
        request.put("password", registrationDto.getPassword());
        request.put("connection", connection);
        request.put("username", registrationDto.getUsername());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            // Use ParameterizedTypeReference to avoid raw type warnings
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                // If Auth0 creation is successful, create user in local DB
                Map<String, Object> body = response.getBody();
                String auth0Id = "auth0|" + body.get("_id"); // Construct Auth0 ID format usually 'auth0|id' or just 'id' depending on response
                // The signup endpoint returns '_id'
                if(auth0Id.startsWith("auth0|auth0|")){
                     auth0Id = auth0Id.replace("auth0|auth0|", "auth0|");
                }
                
                saveUserLocally(auth0Id, registrationDto.getEmail(), registrationDto.getUsername());
                
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("email", registrationDto.getEmail());
                responseMap.put("password", registrationDto.getPassword());
                responseMap.put("message", "User registered successfully");
                return responseMap;
            }
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
        return null;
    }

    private User saveUserLocally(String auth0Id, String email, String username) {
        // Check if user already exists
        Optional<User> existingUser = authRepository.findByAuth0Id(auth0Id);

        if (existingUser.isPresent()) {
            // Update existing user information
            User user = existingUser.get();
            if (email != null) user.setEmail(email);
            if (username != null) user.setUsername(username);
            return authRepository.save(user);
        }

        User newUser = new User();
        newUser.setAuth0Id(auth0Id);
        newUser.setEmail(email);
        // Default username to email if not provided, or handle uniqueness logic
        newUser.setUsername(username != null ? username : email);
        
        return authRepository.save(newUser);
    }

    public Map<String, Object> login(LoginDto loginDto) {
        String url = issuerUri + "oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "http://auth0.com/oauth/grant-type/password-realm");
        map.add("username", loginDto.getEmail());
        map.add("password", loginDto.getPassword());
        map.add("audience", audience);
        map.add("scope", "openid profile email");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("realm", connection);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            // Use ParameterizedTypeReference to avoid raw type warnings
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }
}
