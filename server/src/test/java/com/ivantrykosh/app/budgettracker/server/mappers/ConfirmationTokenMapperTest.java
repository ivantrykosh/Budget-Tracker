package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.ConfirmationTokenDto;
import com.ivantrykosh.app.budgettracker.server.model.ConfirmationToken;
import com.ivantrykosh.app.budgettracker.server.model.User;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test ConfirmationTokenMapper
 */
class ConfirmationTokenMapperTest {

    /**
     * Test converting from valid ConfirmationToken to ConfirmationTokenDto
     */
    @Test
    void convertValidConfirmationTokenToConfirmationTokenDto() {
        ConfirmationToken confirmationToken = createConfirmationToken();

        Mapper<ConfirmationToken, ConfirmationTokenDto> mapper = new ConfirmationTokenMapper();

        // Convert to DTO
        ConfirmationTokenDto confirmationTokenDto = mapper.convertToDto(confirmationToken);

        // Assertions
        assertEquals(confirmationToken.getConfirmationTokenId(), confirmationTokenDto.getConfirmationTokenId(), "ConfirmationToken IDs are not equals!");
        assertEquals(confirmationToken.getConfirmationToken(), confirmationTokenDto.getConfirmationToken(), "Confirmation tokens are not equals!");
        assertEquals(confirmationToken.getCreatedAt(), confirmationTokenDto.getCreatedAt(), "ConfirmationToken CreatedAts are not equals!");
        assertEquals(confirmationToken.getExpiresAt(), confirmationTokenDto.getExpiresAt(), "ConfirmationToken ExpiresAts are not equals!");
        assertEquals(confirmationToken.getConfirmedAt(), confirmationTokenDto.getConfirmedAt(), "ConfirmationToken ConfirmedAts are not equals!");
        assertEquals(confirmationToken.getUser().getUserId(), confirmationTokenDto.getUserId(), "User IDs are not equals!");
    }

    /**
     * Test converting from null ConfirmationToken to ConfirmationTokenDto
     */
    @Test
    void convertNullConfirmationTokenToConfirmationTokenDto() {
        ConfirmationToken confirmationToken = null;

        Mapper<ConfirmationToken, ConfirmationTokenDto> mapper = new ConfirmationTokenMapper();

        // Convert to DTO
        ConfirmationTokenDto confirmationTokenDto = mapper.convertToDto(confirmationToken);

        // Assertion
        assertNull(confirmationTokenDto, "ConfirmationTokenDto is not null!");
    }

    /**
     * Test converting from valid ConfirmationTokenDto to ConfirmationToken
     */
    @Test
    void convertValidConfirmationTokenDtoToConfirmationToken() {
        ConfirmationTokenDto confirmationTokenDto = createConfirmationTokenDto();

        Mapper<ConfirmationToken, ConfirmationTokenDto> mapper = new ConfirmationTokenMapper();

        // Convert to entity
        ConfirmationToken confirmationToken = mapper.convertToEntity(confirmationTokenDto);

        // Assertions
        assertEquals(confirmationTokenDto.getConfirmationTokenId(), confirmationToken.getConfirmationTokenId(), "ConfirmationToken IDs are not equals!");
        assertEquals(confirmationTokenDto.getConfirmationToken(), confirmationToken.getConfirmationToken(), "Confirmation tokens are not equals!");
        assertEquals(confirmationTokenDto.getCreatedAt(), confirmationToken.getCreatedAt(), "ConfirmationToken CreatedAts are not equals!");
        assertEquals(confirmationTokenDto.getExpiresAt(), confirmationToken.getExpiresAt(), "ConfirmationToken ExpiresAts are not equals!");
        assertEquals(confirmationTokenDto.getConfirmedAt(), confirmationToken.getConfirmedAt(), "ConfirmationToken ConfirmedAts are not equals!");
        assertEquals(confirmationTokenDto.getUserId(), confirmationToken.getUser().getUserId(), "User IDs are not equals!");
    }

    /**
     * Test converting from null ConfirmationTokenDto to ConfirmationToken
     */
    @Test
    void convertNullConfirmationTokenDtoToConfirmationToken() {
        ConfirmationToken confirmationTokenDto = null;

        Mapper<ConfirmationToken, ConfirmationTokenDto> mapper = new ConfirmationTokenMapper();

        // Convert to DTO
        ConfirmationTokenDto confirmationToken = mapper.convertToDto(confirmationTokenDto);

        // Assertion
        assertNull(confirmationToken, "ConfirmationToken is not null!");
    }

    /**
     * Create new valid ConfirmationToken
     * @return new valid ConfirmationToken
     */
    private ConfirmationToken createConfirmationToken() {
        // User
        User user = new User();
        user.setUserId(1L);

        // Confirmation token data
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmationTokenId(1L);
        confirmationToken.setConfirmationToken("testToken");
        confirmationToken.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        confirmationToken.setExpiresAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusDays(15L)));
        confirmationToken.setConfirmedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(2L)));
        confirmationToken.setUser(user);

        return confirmationToken;
    }

    /**
     * Create new valid ConfirmationTokenDto
     * @return new valid ConfirmationTokenDto
     */
    private ConfirmationTokenDto createConfirmationTokenDto() {
        // Confirmation token data
        ConfirmationTokenDto confirmationTokenDto = new ConfirmationTokenDto();
        confirmationTokenDto.setConfirmationTokenId(1L);
        confirmationTokenDto.setConfirmationToken("testToken");
        confirmationTokenDto.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        confirmationTokenDto.setExpiresAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusDays(15L)));
        confirmationTokenDto.setConfirmedAt(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(2L)));
        confirmationTokenDto.setUserId(1L);

        return confirmationTokenDto;
    }
}