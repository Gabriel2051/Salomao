package com.biblioteca.salomao.security;

import com.biblioteca.salomao.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Defesa em profundidade anti brute-force (complementa o RateLimitFilter):
 * 5 senhas erradas bloqueiam a conta por 15 minutos. Sucesso zera o contador.
 * Eventos vao para o log de seguranca (sem expor detalhes na interface).
 */
@Component
public class LoginHandlers {

    private static final Logger log = LoggerFactory.getLogger(LoginHandlers.class);
    private static final int MAX_FALHAS = 5;
    private static final int BLOQUEIO_MINUTOS = 15;

    private final UserRepository users;

    public LoginHandlers(UserRepository users) {
        this.users = users;
    }

    public AuthenticationSuccessHandler sucesso() {
        return (request, response, authentication) -> {
            String login = authentication.getName();
            users.findByUsernameIgnoreCaseOrEmailIgnoreCase(login, login).ifPresent(u -> {
                u.setFailedAttempts(0);
                u.setLockedUntil(null);
                users.save(u);
            });
            response.sendRedirect(request.getContextPath() + "/dashboard");
        };
    }

    public AuthenticationFailureHandler falha() {
        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                                AuthenticationException exception)
                    throws IOException, ServletException {
                String login = request.getParameter("login");
                String destino = "/login?erro=1";
                if (login != null && !login.isBlank()) {
                    String v = login.trim();
                    var user = users.findByUsernameIgnoreCaseOrEmailIgnoreCase(v, v);
                    if (user.isPresent()) {
                        var u = user.get();
                        if (exception instanceof LockedException || u.isLocked()) {
                            destino = "/login?bloqueada=1";
                            log.warn("Tentativa de login em conta bloqueada: {}", u.getUsername());
                        } else {
                            int falhas = u.getFailedAttempts() + 1;
                            u.setFailedAttempts(falhas);
                            if (falhas >= MAX_FALHAS) {
                                u.setLockedUntil(Instant.now().plus(BLOQUEIO_MINUTOS, ChronoUnit.MINUTES));
                                destino = "/login?bloqueada=1";
                                log.warn("Conta bloqueada por brute-force: {}", u.getUsername());
                            } else {
                                log.info("Falha de login ({}/{}): {}", falhas, MAX_FALHAS, u.getUsername());
                            }
                            users.save(u);
                        }
                    } else {
                        // Usuario inexistente: mesma resposta generica (nao enumera contas)
                        log.info("Falha de login: conta inexistente tentada.");
                    }
                }
                response.sendRedirect(request.getContextPath() + destino);
            }
        };
    }
}
