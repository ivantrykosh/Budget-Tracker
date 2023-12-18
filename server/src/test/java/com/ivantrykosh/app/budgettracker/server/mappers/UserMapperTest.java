package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.application.dtos.UserDto;
import com.ivantrykosh.app.budgettracker.server.application.mappers.Mapper;
import com.ivantrykosh.app.budgettracker.server.application.mappers.UserMapper;
import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test UserMapper
 */
class UserMapperTest {

    /**
     * Test converting from valid User to UserDto
     */
    @Test
    void convertValidUserToUserDto() {
        User user = createUser();

        Mapper<User, UserDto> mapper = new UserMapper();

        // Convert to DTO
        UserDto userDto = mapper.convertToDto(user);

        // Assertions
        assertEquals(user.getUserId(), userDto.getUserId(), "User IDs are not equals!");
        assertEquals(user.getEmail(), userDto.getEmail(), "User emails are not equals!");
        assertEquals(user.getRegistrationDate(), userDto.getRegistrationDate(), "User registration dates are not equals!");
        assertEquals(user.getIsVerified(), userDto.getIsVerified(), "User IsVerifieds are not equals!");
    }

    /**
     * Test converting from null User to UserDto
     */
    @Test
    void convertNullUserToUserDto() {
        User user = null;

        Mapper<User, UserDto> mapper = new UserMapper();

        // Convert to DTO
        UserDto userDto = mapper.convertToDto(user);

        // Assertion
        assertNull(userDto, "UserDto is not null!");
    }

    /**
     * Test converting from valid UserDto to User
     */
    @Test
    void convertValidUserDtoToUser() {
        UserDto userDto = createUserDto();

        Mapper<User, UserDto> mapper = new UserMapper();

        // Convert to entity
        User user = mapper.convertToEntity(userDto);

        // Assertions
        assertEquals(userDto.getUserId(), user.getUserId(), "User IDs are not equals!");
        assertEquals(userDto.getEmail(), user.getEmail(), "User emails are not equals!");
        assertEquals(userDto.getRegistrationDate(), user.getRegistrationDate(), "User registration dates are not equals!");
        assertEquals(userDto.getIsVerified(), user.getIsVerified(), "User IsVerifieds are not equals!");
    }

    /**
     * Test converting from null UserDto to User
     */
    @Test
    void convertNullUserDtoToUser() {
        UserDto userDto = null;

        Mapper<User, UserDto> mapper = new UserMapper();

        // Convert to entity
        User user = mapper.convertToEntity(userDto);

        // Assertion
        assertNull(user, "User is not null!");
    }

    /**
     * Create new valid User
     * @return new valid User
     */
    private User createUser() {
        // User data
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@email.com");
        user.setPasswordHash("hash");
        user.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        user.setIsVerified(false);

        return user;
    }

    /**
     * Create new valid UserDto
     * @return new valid UserDto
     */
    private UserDto createUserDto() {
        // UserDto data
        UserDto userDto = new UserDto();
        userDto.setUserId(1L);
        userDto.setEmail("test@email.com");
        userDto.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        userDto.setIsVerified(false);

        return userDto;
    }
}