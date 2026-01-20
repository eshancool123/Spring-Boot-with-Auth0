package com.app.auth0.service;

import com.app.auth0.model.User;
import com.app.auth0.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String auth0Id, String email, String username) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByAuth0Id(auth0Id);

        if (existingUser.isPresent()) {
            // Update existing user information
            User user = existingUser.get();
            if (email != null) user.setEmail(email);
            if (username != null) user.setUsername(username);
            return userRepository.save(user);
        }

        User newUser = new User();
        newUser.setAuth0Id(auth0Id);
        newUser.setEmail(email);
        // Default username to email if not provided, or handle uniqueness logic
        newUser.setUsername(username != null ? username : email);
        
        return userRepository.save(newUser);
    }
}
