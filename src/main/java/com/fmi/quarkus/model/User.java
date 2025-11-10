package com.fmi.quarkus.model;

import com.fmi.quarkus.model.Role;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.Username;
import io.quarkus.security.jpa.UserDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@UserDefinition // enables Quarkus Security JPA for form login
public class User extends PanacheEntity {

    @Username
    @Column(unique = true, nullable = false)
    public String username;

    @Column(unique = true, nullable = false)
    public String email;

    @Password // store a bcrypt hash
    @Column(nullable = false)
    public String password;

    @Roles
    @Enumerated(EnumType.STRING)
    public Role role = Role.USER;

    public static User findByUsername(String u) { return find("username", u).firstResult(); }
    public static boolean existsByUsername(String u) { return find("username", u).firstResult() != null; }
    public static boolean existsByEmail(String e) { return find("email", e).firstResult() != null; }
}

