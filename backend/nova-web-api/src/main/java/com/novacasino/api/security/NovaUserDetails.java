package com.novacasino.api.security;

import com.novacasino.infrastructure.persistence.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Adapter between {@link UserEntity} and Spring Security's {@link UserDetails} contract. */
public class NovaUserDetails implements UserDetails {

    private final UserEntity user;

    public NovaUserDetails(final UserEntity user) {
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

    /** The wrapped domain user (exposed so controllers can read id/operatorId). */
    public UserEntity getUser() {
        return user;
    }
}
