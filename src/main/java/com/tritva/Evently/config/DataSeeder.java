package com.tritva.Evently.config;

import com.tritva.Evently.model.Role;
import com.tritva.Evently.model.entity.User;

import com.tritva.Evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdminUser();
    }

    private void seedAdminUser() {
        String adminEmail = "bonnienjuguna106@gmail.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Bonface Njuguna ")
                    .role(Role.ADMIN)
                    .emailVerified(true)
                    .build();

            userRepository.save(adminUser);
            log.info("=================================");
            log.info("Admin user created successfully!");
            log.info("Email: {}", adminEmail);
            log.info("Password: admin123");
            log.info("=================================");
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }
    }
}