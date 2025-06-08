package com.hoz.back.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class UserPrinciple implements UserDetails {

    private final Users user;

    public UserPrinciple(Users user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole();
        System.out.println("User role before processing: " + role);
        
        // Add ROLE_ prefix if not present
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        System.out.println("User role after processing: " + role);
        
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        System.out.println("Created authority: " + authority.getAuthority());
        
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Users getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }
}
