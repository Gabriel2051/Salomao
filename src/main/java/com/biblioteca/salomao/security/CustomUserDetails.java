package com.biblioteca.salomao.security;
import com.biblioteca.salomao.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
public class CustomUserDetails implements UserDetails {
    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final boolean locked;
    private final String displayName;
    public CustomUserDetails(User u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.email = u.getEmail();
        this.passwordHash = u.getPasswordHash();
        this.enabled = u.isEnabled();
        this.locked = u.isLocked();
        this.displayName = u.getDisplayName() != null ? u.getDisplayName() : u.getUsername();
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_USER")); }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !locked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
