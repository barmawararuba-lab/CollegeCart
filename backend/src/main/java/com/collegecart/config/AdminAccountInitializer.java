
package com.collegecart.config;

import com.collegecart.entity.User;
import com.collegecart.enums.Role;
import com.collegecart.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${ADMIN_FULL_NAME:}")
    private String adminFullName;

    public AdminAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (adminEmail.isBlank()
                || adminPassword.isBlank()
                || adminFullName.isBlank()) {

            System.out.println(
                    "Admin account setup skipped: environment variables are missing."
            );
            return;
        }

        String email = adminEmail.trim().toLowerCase(Locale.ROOT);

        User existingUser = userRepository.findByEmail(email)
                .orElse(null);

        if (existingUser != null) {

            if (existingUser.getRole() != Role.ADMIN) {
                throw new IllegalStateException(
                        "Admin email already belongs to a non-admin account."
                );
            }

            System.out.println("Admin account already exists.");
            return;
        }

        User admin = new User();

        admin.setFullName(adminFullName.trim());
        admin.setEmail(email);
        admin.setPasswordHash(
                passwordEncoder.encode(adminPassword)
        );
        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        userRepository.save(admin);

        System.out.println("Initial admin account created successfully.");
    }
}
