package com.ivantrykosh.app.budgettracker.server.application.services;

import com.ivantrykosh.app.budgettracker.server.domain.model.ConfirmationToken;
import com.ivantrykosh.app.budgettracker.server.domain.repos.ConfirmationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing ConfirmationToken entities.
 */
@Service
public class ConfirmationTokenService {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    /**
     * Saves a confirmation token to the database.
     *
     * @param confirmationToken The confirmation token to be saved.
     * @return The saved confirmation token.
     */
    public ConfirmationToken saveConfirmationToken(ConfirmationToken confirmationToken) {
        return confirmationTokenRepository.save(confirmationToken);
    }

    /**
     * Retrieves a confirmation token by its ID.
     *
     * @param confirmationTokenId The ID of the confirmation token to retrieve.
     * @return The confirmation token if found, otherwise null.
     */
    public ConfirmationToken getConfirmationTokenById(Long confirmationTokenId) {
        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findById(confirmationTokenId);
        return confirmationToken.orElse(null);
    }

    /**
     * Retrieves a confirmation token entity by its confirmation token.
     *
     * @param token The confirmation token of the confirmation token entity to retrieve.
     * @return The confirmation token entity if found, otherwise null.
     */
    public ConfirmationToken getConfirmationTokenByConfirmationToken(String token) {
        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByConfirmationToken(token);
        return confirmationToken.orElse(null);
    }

    /**
     * Retrieves all confirmation tokens by userId
     * @param userId The ID of the user to retrieve
     * @return The list of confirmation tokens
     */
    public List<ConfirmationToken> getConfirmationTokensByUserId(Long userId) {
        return confirmationTokenRepository.findAllByUserUserIdOrderByConfirmationTokenIdDesc(userId);
    }

    /**
     * Updates a confirmation token in the database.
     *
     * @param confirmationToken The confirmation token to be updated.
     * @return The updated confirmation token.
     */
    public ConfirmationToken updateConfirmationToken(ConfirmationToken confirmationToken) {
        return confirmationTokenRepository.save(confirmationToken);
    }

    /**
     * Deletes a confirmation token by their ID.
     *
     * @param confirmationTokenId The ID of the confirmation token to delete.
     * @return The deleted confirmation token if found, otherwise null.
     */
    public ConfirmationToken deleteConfirmationTokenById(Long confirmationTokenId) {
        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findById(confirmationTokenId);
        confirmationTokenRepository.deleteById(confirmationTokenId);
        return confirmationToken.orElse(null);
    }

    /**
     * Deletes confirmation tokens by their user ID.
     *
     * @param userId The user ID, by which confirmation tokens are deleted.
     * @return The deleted confirmation tokens.
     */
    public List<ConfirmationToken> deleteConfirmationTokensByUserId(Long userId) {
        List<ConfirmationToken> confirmationTokens = getConfirmationTokensByUserId(userId);
        confirmationTokenRepository.deleteAll(confirmationTokens);
        confirmationTokens.sort((t1, t2) -> Long.compare(t2.getConfirmationTokenId(), t1.getConfirmationTokenId()));
        return confirmationTokens;
    }
}
