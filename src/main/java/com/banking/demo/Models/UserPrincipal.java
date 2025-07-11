package com.banking.demo.Models;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Account account;

    public UserPrincipal(Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole()));
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getEmail(); // used as login username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // you can change this based on account status
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // you can customize this for account lock logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // for password expiration policy
    }

    @Override
    public boolean isEnabled() {
        return true; // for enabling/disabling accounts
    }
}
