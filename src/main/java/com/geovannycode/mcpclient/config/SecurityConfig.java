package com.geovannycode.mcpclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ACTUATOR_PATH = "/actuator/**";
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    private static final String ACTUATOR_INFO_PATH = "/actuator/info";
    private static final String CHAT_PATH = "/chat/**";
    private static final String HR_ASSISTANT_PATH = "/hr-assistant/**";

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(this::configureAuthorization)
                .httpBasic(httpBasic -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }


    private void configureAuthorization(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
                    <HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {

        authz
                .requestMatchers(ACTUATOR_HEALTH_PATH, ACTUATOR_INFO_PATH).permitAll()
                .requestMatchers(HttpMethod.GET, CHAT_PATH).permitAll()


                .requestMatchers(HR_ASSISTANT_PATH).authenticated()
                .requestMatchers(HttpMethod.POST, CHAT_PATH).authenticated()

                .requestMatchers(ACTUATOR_PATH).hasRole(ROLE_ADMIN)

                .anyRequest().authenticated();
    }
}
