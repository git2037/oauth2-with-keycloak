package com.example.resourceserver.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

public class APIUtils {

    private static final Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    public static String getUserID() {
        return jwt.getClaimAsString("sub");
    }

    public String getEmail() {
        return jwt.getClaimAsString("email");
    }
}
