package com.ivantrykosh.app.budgettracker.server.util;

import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import com.ivantrykosh.app.budgettracker.server.application.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class); // Logger

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
            logger.error("Could not find user with email " + username +  "!");
            throw new UsernameNotFoundException("Could not find user with email " + username +  "!");
        }
        return new CustomUserDetails(user);
    }
}
