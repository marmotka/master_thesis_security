package com.fmi.quarkus.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
@UserDefinition
public class User extends PanacheEntity {

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    @Username
    @Column(nullable = false)
    public String username;

    @Column(unique = true, nullable = false)
    public String email;

    @Password // store a bcrypt hash
    @Column(nullable = false)
    public String password;

    @Enumerated(EnumType.STRING)
    public Role role = Role.USER;

    @Roles
    public String getRole() {
        return this.role.name();
    }

    public static User findByUsername(String u) {
        return find(USERNAME, u).firstResult();
    }

    public static boolean existsByUsername(String u) {
        return find(USERNAME, u).firstResult() != null;
    }

    public static boolean existsByEmail(String e) {
        return find(EMAIL, e).firstResult() != null;
    }
}

