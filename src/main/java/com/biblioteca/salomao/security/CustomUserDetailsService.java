package com.biblioteca.salomao.security;
import com.biblioteca.salomao.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }
    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        String v = login == null ? "" : login.trim();
        return users.findByUsernameIgnoreCaseOrEmailIgnoreCase(v, v)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas"));
    }
}
