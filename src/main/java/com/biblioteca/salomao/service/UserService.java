package com.biblioteca.salomao.service;

import com.biblioteca.salomao.domain.User;
import com.biblioteca.salomao.repository.UserRepository;
import com.biblioteca.salomao.util.Sanitizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Registro, validacao de senha forte e manutencao de conta.
 */
@Service
public class UserService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // minimo 8 chars, 1 maiuscula, 1 minuscula, 1 digito
    private static final Pattern STRONG = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,40}$");

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public record Registration(String username, String email, String password, String confirm) {}

    @Transactional
    public User register(Registration r) {
        String username = r.username() == null ? "" : r.username().trim();
        String email = r.email() == null ? "" : r.email().trim().toLowerCase();
        String password = r.password() == null ? "" : r.password();

        if (!USERNAME.matcher(username).matches())
            throw new IllegalArgumentException("Nome de usuário inválido: use 3 a 40 caracteres (letras, números, ponto, _ ou -).");
        if (!EMAIL.matcher(email).matches())
            throw new IllegalArgumentException("E-mail inválido.");
        if (!STRONG.matcher(password).matches())
            throw new IllegalArgumentException("Senha fraca: mínimo 8 caracteres, com maiúscula, minúscula e número.");
        if (!password.equals(r.confirm()))
            throw new IllegalArgumentException("A confirmação de senha não confere.");
        if (users.existsByUsernameIgnoreCase(username))
            throw new IllegalArgumentException("Este nome de usuário já está em uso.");
        if (users.existsByEmailIgnoreCase(email))
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");

        User u = new User();
        u.setUsername(Sanitizer.text(username));
        u.setEmail(email);
        u.setDisplayName(Sanitizer.text(username));
        u.setPasswordHash(encoder.encode(password));
        u.setEnabled(true);
        return users.save(u);
    }

    @Transactional(readOnly = true)
    public User require(UUID id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    @Transactional
    public void changePassword(UUID userId, String current, String next, String confirm) {
        User u = require(userId);
        if (!encoder.matches(current == null ? "" : current, u.getPasswordHash()))
            throw new IllegalArgumentException("Senha atual incorreta.");
        if (!STRONG.matcher(next == null ? "" : next).matches())
            throw new IllegalArgumentException("Nova senha fraca: mínimo 8 caracteres, com maiúscula, minúscula e número.");
        if (!next.equals(confirm))
            throw new IllegalArgumentException("A confirmação da nova senha não confere.");
        u.setPasswordHash(encoder.encode(next));
        users.save(u);
    }

    @Transactional
    public void updateProfile(UUID userId, String displayName) {
        User u = require(userId);
        String clean = Sanitizer.text(displayName);
        u.setDisplayName(clean == null || clean.isBlank() ? u.getUsername() : clean.substring(0, Math.min(80, clean.length())));
        users.save(u);
    }
}
