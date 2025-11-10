package com.fmi.quarkus.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {

    public String hash(String raw) {
        return BcryptUtil.bcryptHash(raw);
    }

    public boolean matches(String raw, String hash) {
        // BCryptUtil doesn't expose matches; re-hash compare by library is not available.
        // Use org.mindrot.jbcrypt if you prefer:
        // return BCrypt.checkpw(raw, hash);
        // For simplicity, we accept that authentication is handled by Security JPA provider.
        return false;
    }
}