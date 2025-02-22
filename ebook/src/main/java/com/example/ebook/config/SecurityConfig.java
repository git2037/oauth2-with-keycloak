package com.example.ebook.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.Header;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.*;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        request -> request
                                .anyRequest().permitAll()
                )
                .oauth2Login(
                        oauth2Login -> oauth2Login.userInfoEndpoint(
                                endpoint -> endpoint.userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                ).build();
    }

    @Bean
    OAuth2AuthorizedClientManager oauth2AuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository oauth2AuthorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                        oauth2AuthorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager oauth2AuthorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(oauth2AuthorizedClientManager);

        final int size = 500 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .apply(oauth2Client.oauth2Configuration())
                .baseUrl("http://localhost:9000")
                .build();
    }

    @Bean
    GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    oidcUserAuthority.getIdToken().getClaimAsStringList("roles").forEach(role -> mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                }
            });

            return mappedAuthorities;
        };
    }

//    @Bean
//    GrantedAuthoritiesMapper userAuthoritiesMapper() {
//        return authorities -> {
//            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
//
//            authorities.forEach(authority -> {
//                if (authority instanceof OidcUserAuthority oidcAuth) {
//                    oidcAuth.getIdToken().getClaimAsStringList("roles")
//                            .forEach(a -> mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + a)));
//                } else if (authority instanceof OAuth2UserAuthority oauth2Auth) {
//                    Map<String, Object> realmAccessMap = oauth2Auth.getAttributes();
//                    if (realmAccessMap != null && realmAccessMap.containsKey("roles")) {
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        List<String> keycloakRoles = objectMapper
//                                .convertValue(realmAccessMap.get("roles"), new TypeReference<>() {});
//                        keycloakRoles.forEach(
//                                role -> mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role))
//                        );
//                    }
//                }
//            });
//
//            return mappedAuthorities;
//        };
//    }

//    @Bean
//    public WebClient webClient(OAuth2AuthorizedClientManager oauth2AuthorizedClientManager) {
//        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
//                new ServletOAuth2AuthorizedClientExchangeFilterFunction(oauth2AuthorizedClientManager);
//
//        return WebClient.builder()
//                .apply(oauth2Client.oauth2Configuration())
//                .baseUrl("http://localhost:9000/")
//                .build();
//    }
}
