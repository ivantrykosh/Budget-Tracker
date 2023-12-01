package com.ivantrykosh.app.budgettracker.server.util;

import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.services.UserService;
import com.ivantrykosh.app.budgettracker.server.util.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    /**
     * Loads user details by username from the database.
     *
     * @param username The username identifying the user whose data is required.
     * @return UserDetails object representing the user.
     * @throws UsernameNotFoundException If the user is not found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("Could not found user!");
        }
        return new CustomUserDetails(user);
    }
}
