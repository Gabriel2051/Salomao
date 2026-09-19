package com.biblioteca.salomao.config;

import com.biblioteca.salomao.security.LoginHandlers;
import com.biblioteca.salomao.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Seguranca: sessoes server-side, BCrypt, CSRF ativo, headers rigidos,
 * rate limiting no login/registro, logout seguro com invalidacao de sessao.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;
    private final LoginHandlers loginHandlers;

    public SecurityConfig(RateLimitFilter rateLimitFilter, LoginHandlers loginHandlers) {
        this.rateLimitFilter = rateLimitFilter;
        this.loginHandlers = loginHandlers;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        // Usa o DaoAuthenticationProvider padrao montado pelo Boot a partir de
        // CustomUserDetailsService + PasswordEncoder (sem bean manual: evita o
        // warning "UserDetailsService beans will not be used...").
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/login", "/registro", "/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("login")
                .successHandler(loginHandlers.sucesso())
                .failureHandler(loginHandlers.falha())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/sair")
                .logoutSuccessUrl("/login?saida=1")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .sessionManagement(sess -> {
                sess.sessionFixation(fix -> fix.migrateSession());
                sess.invalidSessionUrl("/login?expirada=1");
                sess.maximumSessions(3).maxSessionsPreventsLogin(false);
            })
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
                    + "img-src 'self' data:; font-src 'self' data:; connect-src 'self'; "
                    + "frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
            // CSRF mantido ATIVO (protecao padrao do Spring Security para formularios)
            .exceptionHandling(ex -> ex.accessDeniedPage("/erro/403"));
        return http.build();
    }
}
