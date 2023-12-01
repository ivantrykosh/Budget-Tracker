package com.ivantrykosh.app.budgettracker.server.util;

import com.ivantrykosh.app.budgettracker.server.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Custom implementation of UserDetails that extends the model's User class.
 */
public class CustomUserDetails extends User implements UserDetails {
    private String username; // Username (email)
    private String password; // Password
    Collection<? extends GrantedAuthority> authorities; // Authorities (always empty)

    /**
     * Constructs a CustomUserDetails object based on a User entity.
     *
     * @param user The User entity from which to extract details.
     */
    public CustomUserDetails(User user) {
        this.username = user.getEmail();
        this.password= user.getPasswordHash();
        this.authorities = new ArrayList<>();
    }

    /**
     * Returns the authorities (roles or permissions) granted to the user.
     *
     * @return A collection of authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return The password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return The username.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has not expired.
     *
     * @return true if the user's account is valid (not expired), false otherwise.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return true if the user is not locked, false otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have not expired.
     *
     * @return true if the user's credentials are valid (not expired), false otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true if the user is enabled, false otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
