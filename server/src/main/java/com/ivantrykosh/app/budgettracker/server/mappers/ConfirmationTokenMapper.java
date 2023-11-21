package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.ConfirmationTokenDto;
import com.ivantrykosh.app.budgettracker.server.model.ConfirmationToken;
import com.ivantrykosh.app.budgettracker.server.model.User;

/**
 * Mapper for Confirmation Token
 */
public class ConfirmationTokenMapper implements Mapper<ConfirmationToken, ConfirmationTokenDto> {

    /**
     * Convert from ConfirmationToken to ConfirmationTokenDto
     * @param confirmationToken confirmationToken to convert
     * @return ConfirmationTokenDto of confirmationToken
     */
    @Override
    public ConfirmationTokenDto convertToDto(ConfirmationToken confirmationToken) {
        if (confirmationToken == null) {
            return null;
        }

        Long userId = null;
        if (confirmationToken.getUser() != null) {
            userId = confirmationToken.getUser().getUserId();
        }

        ConfirmationTokenDto confirmationTokenDto = new ConfirmationTokenDto();
        confirmationTokenDto.setConfirmationTokenId(confirmationToken.getConfirmationTokenId());
        confirmationTokenDto.setConfirmationToken(confirmationToken.getConfirmationToken());
        confirmationTokenDto.setCreatedAt(confirmationToken.getCreatedAt());
        confirmationTokenDto.setExpiresAt(confirmationToken.getExpiresAt());
        confirmationTokenDto.setConfirmedAt(confirmationToken.getConfirmedAt());
        confirmationTokenDto.setUserId(userId);
        return confirmationTokenDto;
    }

    /**
     * Convert from ConfirmationTokenDto to ConfirmationToken
     * @param confirmationTokenDto confirmationTokenDto to convert
     * @return ConfirmationToken of confirmationTokenDto
     */
    @Override
    public ConfirmationToken convertToEntity(ConfirmationTokenDto confirmationTokenDto) {
        if (confirmationTokenDto == null) {
            return null;
        }

        User user = null;
        if (confirmationTokenDto.getUserId() != null) {
            user = new User();
            user.setUserId(confirmationTokenDto.getUserId());
        }

        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmationTokenId(confirmationTokenDto.getConfirmationTokenId());
        confirmationToken.setConfirmationToken(confirmationTokenDto.getConfirmationToken());
        confirmationToken.setCreatedAt(confirmationTokenDto.getCreatedAt());
        confirmationToken.setExpiresAt(confirmationTokenDto.getExpiresAt());
        confirmationToken.setConfirmedAt(confirmationTokenDto.getConfirmedAt());
        confirmationToken.setUser(user);
        return confirmationToken;
    }
}
