package com.example.resourceserver.config;

import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MyJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@Nonnull Jwt jwt) {
        Collection<GrantedAuthority> authorities = this.getAuthorities(jwt);
        String principalClaimName = "preferred_username";
        String principalClaimValue = jwt.getClaimAsString(principalClaimName);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    public Collection<GrantedAuthority> getAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // get scope in token and set to GrantedAuthority
        List<String> scopes = Arrays.stream(jwt.getClaimAsString("scope").split(" ")).toList();
        scopes.forEach(scope -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope)));

        // get roles claim in token and set to GrantedAuthority
        List<String> roles = jwt.getClaimAsStringList("roles");
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));

        return authorities;
    }

}
