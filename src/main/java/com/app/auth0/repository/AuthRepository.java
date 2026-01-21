package com.app.auth0.repository;

import com.app.auth0.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
    java.util.Optional<User> findByAuth0Id(String auth0Id);
}
