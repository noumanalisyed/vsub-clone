package com.springoot.vsubclone.config;

/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .ignoringAntMatchers("/api/video/generate_video") // Disable CSRF for this endpoint
                .and()
                .authorizeRequests()
                .antMatchers("/api/video/generate_video").permitAll() // Adjust access control as needed
                .anyRequest().authenticated();

        return http.build();
    }
}
*/
