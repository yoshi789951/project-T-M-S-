package com.taskflow.config;

import com.taskflow.model.Role;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.seed.admin.enabled:true}") private boolean enabled;
    @Value("${app.seed.admin.name:TaskFlow Admin}") private String name;
    @Value("${app.seed.admin.email:admin@taskflow.local}") private String email;
    @Value("${app.seed.admin.password:Admin123!}") private String password;

    public DataSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!enabled || users.existsByEmailIgnoreCase(email)) return;
        User admin = new User();
        admin.setName(name);
        admin.setEmail(email);
        admin.setPassword(encoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        users.save(admin);
    }
}
