package com.qq24650393.demo.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AdminProperties properties;
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(AdminProperties properties, AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.seedEnabled() || repository.findByUsername(properties.username()).isPresent()) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(properties.username());
        user.setPasswordHash(passwordEncoder.encode(properties.password()));
        user.setRoles("ROLE_ADMIN");
        user.setEnabled(true);
        repository.insert(user);
    }
}
