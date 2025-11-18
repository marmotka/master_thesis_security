package com.fmi.quarkus.service;


import com.fmi.quarkus.dto.UserDto;
import com.fmi.quarkus.exception.DuplicateFieldException;
import com.fmi.quarkus.exception.PasswordChangeException;
import com.fmi.quarkus.mapper.UserMapper;
import com.fmi.quarkus.model.Role;
import com.fmi.quarkus.model.User;
import com.fmi.quarkus.web.AuthenticationController;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class);

    public static final int PASSWORD_LENGTH = 8;
    public static final String CURRENT_PASSWORD = "currentPassword";
    public static final String NEW_PASSWORD = "newPassword";
    public static final String CONFIRM_PASSWORD = "confirmPassword";
    public static final String USERNAME = "username";

    @Inject
    UserMapper userMapper;

    public Optional<User> findByUsername(String username) {
        return User.find(USERNAME, username).firstResultOptional();
    }

    public List<UserDto> findAllUsersForAdmin() {
        List<User> users = User.listAll();
        return userMapper.toDtoList(users);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(User.findById(id));
    }

    @Transactional
    public void register(AuthenticationController.RegisterRequest request) {
        if (User.existsByEmail(request.email)) throw new DuplicateFieldException("Email is already in use.");
        if (User.existsByUsername(request.username)) throw new DuplicateFieldException("Username is already in use.");
        User u = new User();
        u.username = request.username;
        u.email = request.email;
        u.password = BcryptUtil.bcryptHash(request.password);
        u.role = Role.USER;
        u.persist();
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest req) {
        Map<String, String> errors = new HashMap<>();

        // Basic validation (no passwords in logs or exceptions)
        if (req.currentPassword == null || req.currentPassword.isBlank()) {
            errors.put(CURRENT_PASSWORD, "Please enter your current password.");
        }
        if (req.newPassword == null || req.newPassword.isBlank()) {
            errors.put(NEW_PASSWORD, "Please enter a new password.");
        } else if (req.newPassword.length() < PASSWORD_LENGTH) {
            errors.put(NEW_PASSWORD, "Password must be at least " + PASSWORD_LENGTH + " characters.");
        }
        if (req.confirmPassword == null || req.confirmPassword.isBlank()) {
            errors.put(CONFIRM_PASSWORD, "Please confirm your new password.");
        } else if (!req.confirmPassword.equals(req.newPassword)) {
            errors.put(CONFIRM_PASSWORD, "Passwords do not match.");
        }

        if (!errors.isEmpty()) {
            throw new PasswordChangeException("Validation failed for password change", errors);
        }

        User u = User.find(USERNAME, username).firstResult();
        if (u == null) {
            // This should never happen for an authenticated user, but keep it defensive.
            log.warnf("Password change requested for non-existing user: %s", username);
            throw new PasswordChangeException("User not found", Map.of());
        }

        // Check current password (using hash, no decryption)
        if (!BcryptUtil.matches(req.currentPassword, u.password)) {
            errors.put(CURRENT_PASSWORD, "Current password is incorrect.");
            throw new PasswordChangeException("Current password does not match", errors);
        }

        // All good – update hash
        u.password = BcryptUtil.bcryptHash(req.newPassword);
        u.persist();
        log.infof("Password updated for user '%s'", username);
    }


    @Transactional
    public void deleteUserAsAdmin(Long userId, String currentAdminUsername) {
        User target = User.findById(userId);
        if (target == null) {
            throw new EntityNotFoundException("User not found");
        }

        // Prevent deleting yourself
        if (target.username != null && target.username.equals(currentAdminUsername)) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }

        // Optional: prevent deleting last admin
        if (target.role == Role.ADMIN) {
            long adminCount = User.find("SELECT u FROM User u WHERE ?1 IN elements(u.roles)", "ADMIN")
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user.");
            }
        }

        target.delete(); // or User.deleteById(userId);
        log.infof("Admin '%s' deleted user '%s' (id=%d)",
                currentAdminUsername, target.username, target.id);
    }

}