package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.AccountUsersDto;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.services.UserService;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test AccountUsersMapper
 */
class AccountUsersMapperTest {

    /**
     * Test converting from valid AccountUsers to AccountUsersDto
     */
    @Test
    void convertValidAccountUsersToAccountUsersDto() {
        // Arrange
        UserService userService = mock(UserService.class);
        when(userService.getUserById(2L)).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserById(3L)).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserById(4L)).thenReturn(createUser(4L, "test4@gmail.com"));
        when(userService.getUserByEmail("test2@gmail.com")).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserByEmail("test3@gmail.com")).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserByEmail("test4@gmail.com")).thenReturn(createUser(4L, "test4@gmail.com"));

        AccountUsers accountUsers = createAccountUsers();

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper(userService);

        // Convert to DTO
        AccountUsersDto accountUsersDto = mapper.convertToDto(accountUsers);

        // Assertions
        assertEquals(accountUsers.getAccountUsersId(), accountUsersDto.getAccountUsersId(), "AccountUsers IDs are not equals!");
        assertEquals(accountUsers.getUser2Id(), userService.getUserByEmail(accountUsersDto.getEmail2()).getUserId(), "AccountUsers User2ID are not equals!");
        assertEquals(accountUsers.getUser3Id(), userService.getUserByEmail(accountUsersDto.getEmail3()).getUserId(), "AccountUsers User3ID are not equals!");
        assertEquals(accountUsers.getUser4Id(), userService.getUserByEmail(accountUsersDto.getEmail4()).getUserId(), "AccountUsers User4ID are not equals!");
        assertEquals(accountUsers.getAccount().getAccountId(), accountUsersDto.getAccountId(), "Account IDs are not equals!");
    }

    /**
     * Test converting from null AccountUsers to AccountUsersDto
     */
    @Test
    void convertNullAccountUsersToAccountUsersDto() {
        // Arrange
        UserService userService = mock(UserService.class);
        when(userService.getUserById(2L)).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserById(3L)).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserById(4L)).thenReturn(createUser(4L, "test4@gmail.com"));
        when(userService.getUserByEmail("test2@gmail.com")).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserByEmail("test3@gmail.com")).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserByEmail("test4@gmail.com")).thenReturn(createUser(4L, "test4@gmail.com"));

        AccountUsers accountUsers = null;

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper(userService);

        // Convert to DTO
        AccountUsersDto accountUsersDto = mapper.convertToDto(accountUsers);

        // Assertions
        assertNull(accountUsersDto, "AccountUsersDto is not null!");
    }

    /**
     * Test converting from valid AccountUsersDto to AccountUsers
     */
    @Test
    void convertValidAccountUsersDtoToAccountUsers() {
        // Arrange
        UserService userService = mock(UserService.class);
        when(userService.getUserById(2L)).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserById(3L)).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserById(4L)).thenReturn(createUser(4L, "test4@gmail.com"));
        when(userService.getUserByEmail("test2@gmail.com")).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserByEmail("test3@gmail.com")).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserByEmail("test4@gmail.com")).thenReturn(createUser(4L, "test4@gmail.com"));

        AccountUsersDto accountUsersDto = createAccountUsersDto();

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper(userService);

        // Convert to entity
        AccountUsers accountUsers = mapper.convertToEntity(accountUsersDto);

        // Assertions
        assertEquals(accountUsersDto.getAccountUsersId(), accountUsers.getAccountUsersId(), "AccountUsers IDs are not equals!");
        assertEquals(accountUsers.getUser2Id(), userService.getUserByEmail(accountUsersDto.getEmail2()).getUserId(), "AccountUsers User2ID are not equals!");
        assertEquals(accountUsers.getUser3Id(), userService.getUserByEmail(accountUsersDto.getEmail3()).getUserId(), "AccountUsers User3ID are not equals!");
        assertEquals(accountUsers.getUser4Id(), userService.getUserByEmail(accountUsersDto.getEmail4()).getUserId(), "AccountUsers User4ID are not equals!");
        assertEquals(accountUsersDto.getAccountId(), accountUsers.getAccount().getAccountId(), "Account IDs are not equals!");
    }

    /**
     * Test converting from null AccountUsersDto to AccountUsers
     */
    @Test
    void convertNullAccountUsersDtoToAccountUsers() {
        // Arrange
        UserService userService = mock(UserService.class);
        when(userService.getUserById(2L)).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserById(3L)).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserById(4L)).thenReturn(createUser(4L, "test4@gmail.com"));
        when(userService.getUserByEmail("test2@gmail.com")).thenReturn(createUser(2L, "test2@gmail.com"));
        when(userService.getUserByEmail("test3@gmail.com")).thenReturn(createUser(3L, "test3@gmail.com"));
        when(userService.getUserByEmail("test4@gmail.com")).thenReturn(createUser(4L, "test4@gmail.com"));

        AccountUsersDto accountUsersDto = null;

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper(userService);

        // Convert to entity
        AccountUsers accountUsers = mapper.convertToEntity(accountUsersDto);

        // Assertions
        assertNull(accountUsers, "AccountUsers is not null!");
    }

    /**
     * Create new valid User
     * @param userId User Id
     * @param userEmail User email
     * @return new valid User
     */
    private User createUser(Long userId, String userEmail) {
        // User data
        User user = new User();
        user.setUserId(userId);
        user.setEmail(userEmail);

        return user;
    }

    /**
     * Create new valid AccountUsers
     * @return new valid AccountUsers
     */
    private AccountUsers createAccountUsers() {
        // Account
        Account account = new Account();
        account.setAccountId(1L);

        // AccountUsers data
        AccountUsers accountUsers = new AccountUsers();
        accountUsers.setAccountUsersId(1L);
        accountUsers.setUser2Id(2L);
        accountUsers.setUser3Id(3L);
        accountUsers.setUser4Id(4L);
        accountUsers.setAccount(account);

        return accountUsers;
    }

    /**
     * Create new valid AccountUsersDto
     * @return new valid AccountUsersDto
     */
    private AccountUsersDto createAccountUsersDto() {
        // AccountUsersDto data
        AccountUsersDto accountUsersDto = new AccountUsersDto();
        accountUsersDto.setAccountUsersId(1L);
        accountUsersDto.setEmail2("test2@gmail.com");
        accountUsersDto.setEmail3("test3@gmail.com");
        accountUsersDto.setEmail4("test4@gmail.com");
        accountUsersDto.setAccountId(1L);

        return accountUsersDto;
    }
}