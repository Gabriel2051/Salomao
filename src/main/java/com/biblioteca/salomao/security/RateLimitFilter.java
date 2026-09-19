package com.biblioteca.salomao.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
/** Rate limiting simples em memoria para /login e /registro (anti brute-force). */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    @Value("${salomao.rate-limit.max-attempts:5}")
    private int maxAttempts;
    @Value("${salomao.rate-limit.window-minutes:10}")
    private int windowMinutes;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean target = uri.equals("/login") || uri.equals("/registro");
        return !(target && "POST".equalsIgnoreCase(request.getMethod()));
    }
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String key = clientIp(req);
        Instant now = Instant.now();
        Deque<Instant> dq = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (dq) {
            while (!dq.isEmpty() && dq.peekFirst().isBefore(now.minusSeconds(windowMinutes * 60L))) dq.pollFirst();
            if (dq.size() >= maxAttempts) {
                res.setStatus(429);
                res.setContentType("text/html;charset=UTF-8");
                res.getWriter().write("<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"utf-8\"><title>Salomao — Aguarde</title></head><body style=\"background:#080808;color:#F5F5F5;font-family:sans-serif;padding:48px\"><h1>Muitas tentativas</h1><p>Aguarde alguns minutos antes de tentar novamente.</p><a href=\"/login\" style=\"color:#B5121B\">Voltar ao login</a></body></html>");
                return;
            }
            dq.addLast(now);
        }
        chain.doFilter(req, res);
    }
    private String clientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) return fwd.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
