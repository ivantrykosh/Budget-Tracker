package com.ivantrykosh.app.budgettracker.server.validators;

import com.ivantrykosh.app.budgettracker.server.domain.model.Account;
import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import com.ivantrykosh.app.budgettracker.server.application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for validating account-related information.
 */
@Component
public class AccountValidator {
    @Autowired
    private UserService userService;

    /**
     * Checks whether the given name is valid.
     *
     * @param name The name to validate.
     * @param accounts The user's accounts
     * @return True if the name is valid, false otherwise.
     */
    public boolean checkName(String name, List<Account> accounts) {
        for (Account account : accounts) {
            if (name.equals(account.getName())) {
                return false;
            }
        }
        return name.length() <= 25;
    }

    /**
     * Checks whether the given email is valid (user with this email exists).
     *
     * @param email The user's email
     * @param account The user account
     * @return True if the email is valid, false otherwise.
     */
    public boolean checkEmail(String email, Account account) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        if (account.getUser().getUserId() == user.getUserId()) {
            return false;
        }
        return true;
    }
}
