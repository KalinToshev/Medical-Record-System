package nbu.informatics.medicalRecordSystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/pending").hasRole("PENDING")
                        .requestMatchers("/admin/users/**").hasRole("ADMIN")
                        .requestMatchers("/doctors/**").hasRole("ADMIN")
                        .requestMatchers("/patients/**").hasRole("ADMIN")
                        .requestMatchers("/specialities/**").hasRole("ADMIN")
                        .requestMatchers("/diagnoses/**").hasRole("ADMIN")
                        .requestMatchers("/examinations/new").hasRole("DOCTOR")
                        .requestMatchers("/examinations/*/edit").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/examinations/*/delete").hasRole("ADMIN")
                        .requestMatchers("/examinations").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                        .requestMatchers("/reports/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
