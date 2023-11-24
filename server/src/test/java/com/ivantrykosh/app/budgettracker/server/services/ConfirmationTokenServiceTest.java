package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.model.ConfirmationToken;
import com.ivantrykosh.app.budgettracker.server.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test ConfirmationTokenService
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ConfirmationTokenService.class, UserService.class})
class ConfirmationTokenServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    private User user;

    /**
     * Save user to db
     */
    @BeforeEach
    public void saveUser() {
        User newUser = new User();
        newUser.setEmail("testemail@gmail.com");
        newUser.setPasswordSalt("salt");
        newUser.setPasswordHash("hash");
        newUser.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        newUser.setIsVerified(false);
        user = userService.saveUser(newUser);
    }

    /**
     * Test saving ConfirmationToken
     */
    @Test
    void saveConfirmationToken() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();

        // Save token
        ConfirmationToken savedConfirmationToken = confirmationTokenService.saveConfirmationToken(confirmationToken);

        // Print token and saved token
        System.out.println(confirmationToken + "\n" + savedConfirmationToken);

        // Assert parameters are equals
        assertEquals(confirmationToken.getConfirmationToken(), savedConfirmationToken.getConfirmationToken(), "Tokens are not equals!");
    }

    /**
     * Test saving ConfirmationToken with invalid data
     */
    @Test
    void saveConfirmationTokenWihInvalidData() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();
        confirmationToken.setUser(null);

        System.out.println(confirmationToken);

        assertThrows(Exception.class, () -> confirmationTokenService.saveConfirmationToken(confirmationToken), "Exception was not thrown!");
    }

    /**
     * Test getting ConfirmationToken
     */
    @Test
    void getConfirmationTokenById() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();

        // Save token
        ConfirmationToken savedConfirmationToken = confirmationTokenService.saveConfirmationToken(confirmationToken);

        // Get token by ID
        ConfirmationToken retrievedConfirmationToken = confirmationTokenService.getConfirmationTokenById(savedConfirmationToken.getConfirmationTokenId());

        // Print token, saved token and retrieved token
        System.out.println(confirmationToken + "\n" + savedConfirmationToken + "\n" + retrievedConfirmationToken);

        // Assert parameters are equals
        assertEquals(savedConfirmationToken.getConfirmationToken(), retrievedConfirmationToken.getConfirmationToken(), "Tokens are not equals!");
    }

    /**
     * Test getting ConfirmationToken with invalid ID
     */
    @Test
    void getConfirmationTokenByInvalidId() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();

        // Save token
        ConfirmationToken savedConfirmationToken = confirmationTokenService.saveConfirmationToken(confirmationToken);

        assertNull(confirmationTokenService.getConfirmationTokenById(Long.MAX_VALUE), "Token is not null!");
    }

    /**
     * Test getting confirmation tokens by user ID
     */
    @Test
    void getConfirmationTokenByUserId() {
        ConfirmationToken confirmationToken1 = createNewValidConfirmationToken();

        ConfirmationToken confirmationToken2 = createNewValidConfirmationToken();
        confirmationToken2.setConfirmedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));

        // Save confirmation tokens
        ConfirmationToken savedConfirmationToken1 = confirmationTokenService.saveConfirmationToken(confirmationToken1);
        ConfirmationToken savedConfirmationToken2 = confirmationTokenService.saveConfirmationToken(confirmationToken2);

        // Get confirmation tokens
        List<ConfirmationToken> retrievedConfirmationTokens = confirmationTokenService.getConfirmationTokenByUserId(user.getUserId());

        // Print saved and retrieved confirmation tokens
        System.out.println(savedConfirmationToken1 + "\n" + savedConfirmationToken2 + "\n" + retrievedConfirmationTokens);

        // Assert parameters are equals
        assertEquals(2, retrievedConfirmationTokens.size(), "Size of token's list is not 2!");
        assertEquals(confirmationToken1.getConfirmationToken(), retrievedConfirmationTokens.get(0).getConfirmationToken(), "Tokens are not equals!");
        assertEquals(confirmationToken2.getConfirmationToken(), retrievedConfirmationTokens.get(1).getConfirmationToken(), "Tokens are not equals!");
    }

    /**
     * Test getting tokens with invalid user ID
     */
    @Test
    void getConfirmationTokensByInvalidUserId() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();

        // Save token
        ConfirmationToken savedConfirmationToken = confirmationTokenService.saveConfirmationToken(confirmationToken);

        assertEquals(0, confirmationTokenService.getConfirmationTokenByUserId(Long.MAX_VALUE).size(), "List is not empty!");
    }

    /**
     * Test updating ConfirmationToken
     */
    @Test
    void updateConfirmationToken() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();

        // Save token
        ConfirmationToken savedConfirmationToken = confirmationTokenService.saveConfirmationToken(confirmationToken);

        // Update confirmedAt
        savedConfirmationToken.setConfirmedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));

        // Update token
        ConfirmationToken updatedConfirmationToken = confirmationTokenService.updateConfirmationToken(savedConfirmationToken);

        // Print token and updatedToken
        System.out.println(savedConfirmationToken + "\n" + updatedConfirmationToken);

        // Assert parameters are equals
        assertEquals(savedConfirmationToken.getConfirmationToken(), updatedConfirmationToken.getConfirmationToken(), "Tokens are not equals!");
        assertEquals(savedConfirmationToken.getConfirmedAt(), updatedConfirmationToken.getConfirmedAt(), "ConfirmedAts are not equals!");
    }

    /**
     * Test deleting ConfirmationToken
     */
    @Test
    void deleteConfirmationTokenById() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();

        // Save token
        ConfirmationToken savedConfirmationToken = confirmationTokenService.saveConfirmationToken(confirmationToken);

        // Delete token
        ConfirmationToken deletedConfirmationToken = confirmationTokenService.deleteConfirmationTokenById(savedConfirmationToken.getConfirmationTokenId());

        // Print token and deleted token
        System.out.println(savedConfirmationToken + "\n" + deletedConfirmationToken);

        // Assert parameters are equals
        assertEquals(savedConfirmationToken.getConfirmationToken(), deletedConfirmationToken.getConfirmationToken(), "Tokens are not equals!");
        assertNull(confirmationTokenService.getConfirmationTokenById(savedConfirmationToken.getConfirmationTokenId()), "Token is not deleted!");
    }

    /**
     * Test deleting not existing ConfirmationToken
     */
    @Test
    void deleteNotExistingConfirmationTokenById() {
        ConfirmationToken confirmationToken = createNewValidConfirmationToken();

        // Save token
        ConfirmationToken savedConfirmationToken = confirmationTokenService.saveConfirmationToken(confirmationToken);

        assertNull(confirmationTokenService.getConfirmationTokenById(Long.MAX_VALUE), "Token is deleted!");

    }

    /**
     * Create new valid ConfirmationToken
     * @return new valid ConfirmationToken
     */
    private ConfirmationToken createNewValidConfirmationToken() {
        // Confirmation token
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmationToken("token");
        confirmationToken.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        confirmationToken.setExpiresAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusDays(15L)));
        confirmationToken.setConfirmedAt(null);
        confirmationToken.setUser(user);

        return confirmationToken;
    }
}