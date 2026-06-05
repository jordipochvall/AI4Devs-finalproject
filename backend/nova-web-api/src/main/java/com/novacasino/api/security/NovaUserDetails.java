package com.novacasino.api.security;

import com.novacasino.infrastructure.persistence.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Adapter entre {@link UserEntity} y el contrato de Spring Security {@link UserDetails}. */
public class NovaUserDetails implements UserDetails {

    private final UserEntity user;

    public NovaUserDetails(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override public String getPassword()  { return user.getPasswordHash(); }
    @Override public String getUsername()  { return user.getEmail(); }
    @Override public boolean isEnabled()   { return user.isActive(); }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }

    public UserEntity getUser() { return user; }
}
