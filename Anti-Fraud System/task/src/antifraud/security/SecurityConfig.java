package antifraud.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfig(AuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)                           // For modifying requests via Postman
                .exceptionHandling(handing -> handing
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                )
                .headers(headers -> headers.frameOptions().disable())           // for Postman, the H2 console
                .authorizeHttpRequests(requests -> requests                     // manage access
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()      // needs to run test
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/stolencard/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/stolencard/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history/**").hasRole("SUPPORT")
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                // other configurations
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
