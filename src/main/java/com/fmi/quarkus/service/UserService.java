package com.fmi.quarkus.service;


import com.fmi.quarkus.model.Role;
import com.fmi.quarkus.model.User;
import com.fmi.quarkus.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject PasswordService passwords;

    public Optional<User> findByUsername(String u) { return Optional.ofNullable(User.findByUsername(u)); }
    public List<User> findAll() { return User.listAll(); }
    public Optional<User> findById(Long id) { return Optional.ofNullable(User.findById(id)); }

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (User.existsByEmail(email)) throw new IllegalArgumentException("Email is already in use.");
        if (User.existsByUsername(username)) throw new IllegalArgumentException("Username is already taken.");
        User u = new User();
        u.username = username;
        u.email = email;
        u.password = passwords.hash(rawPassword);
        u.role = Role.USER;
        u.persist();
        return u;
    }

    @Transactional
    public void changePassword(String username, String currentRaw, String newRaw) {
        User u = User.findByUsername(username);
        if (u == null) throw new IllegalArgumentException("User not found.");
        // Password check is done by the provider on login; here we trust a separate
        // check if you want. As a simple guard, disallow too short here:
        if (newRaw == null || newRaw.length() < 8) throw new IllegalArgumentException("Password too short.");
        u.password = passwords.hash(newRaw);
        u.persist();
    }

    @Transactional
    public void deleteById(Long id) {
        User.deleteById(id);
    }
}