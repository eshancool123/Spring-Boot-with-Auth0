package com.app.auth0;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class Auth0Application {

	public static void main(String[] args) {
		SpringApplication.run(Auth0Application.class, args);
	}

	@Bean
	public CommandLineRunner checkDbConnection(DataSource dataSource) {
		return args -> {
			try (Connection connection = dataSource.getConnection()) {
				System.out.println("✅ Database connected successfully!");
				System.out.println("Schema: " + connection.getSchema());
			} catch (Exception e) {
				System.err.println("❌ Database connection failed: " + e.getMessage());
			}
		};
	}

}
