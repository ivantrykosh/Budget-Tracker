package com.ivantrykosh.app.budgettracker.server.application.services;

import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import com.ivantrykosh.app.budgettracker.server.domain.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing User entities.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Saves a user to the database.
     *
     * @param user The user to be saved.
     * @return The saved user.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The user if found, otherwise null.
     */
    public User getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email The email of the user to retrieve.
     * @return The user if found, otherwise null.
     */
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    /**
     * Updates a user in the database.
     *
     * @param user The user to be updated.
     * @return The updated user.
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     * @return The deleted user if found, otherwise null.
     */
    public User deleteUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        userRepository.deleteById(userId);
        return user.orElse(null);
    }
}
