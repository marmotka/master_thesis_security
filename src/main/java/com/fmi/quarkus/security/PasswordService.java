package com.fmi.quarkus.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {

    public String hash(String raw) {
        return BcryptUtil.bcryptHash(raw);
    }


}