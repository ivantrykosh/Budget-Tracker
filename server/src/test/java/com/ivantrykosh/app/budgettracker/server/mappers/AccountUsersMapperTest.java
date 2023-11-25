package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.AccountUsersDto;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test AccountUsersMapper
 */
class AccountUsersMapperTest {

    /**
     * Test converting from valid AccountUsers to AccountUsersDto
     */
    @Test
    void convertValidAccountUsersToAccountUsersDto() {
        AccountUsers accountUsers = createAccountUsers();

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper();

        // Convert to DTO
        AccountUsersDto accountUsersDto = mapper.convertToDto(accountUsers);

        // Assertions
        assertEquals(accountUsers.getAccountUsersId(), accountUsersDto.getAccountUsersId(), "AccountUsers IDs are not equals!");
        assertEquals(accountUsers.getUser2Id(), accountUsersDto.getUser2Id(), "AccountUsers User2ID are not equals!");
        assertEquals(accountUsers.getUser3Id(), accountUsersDto.getUser3Id(), "AccountUsers User3ID are not equals!");
        assertEquals(accountUsers.getUser4Id(), accountUsersDto.getUser4Id(), "AccountUsers User4ID are not equals!");
        assertEquals(accountUsers.getAccount().getAccountId(), accountUsersDto.getAccountId(), "Account IDs are not equals!");
    }

    /**
     * Test converting from null AccountUsers to AccountUsersDto
     */
    @Test
    void convertNullAccountUsersToAccountUsersDto() {
        AccountUsers accountUsers = null;

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper();

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
        AccountUsersDto accountUsersDto = createAccountUsersDto();

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper();

        // Convert to entity
        AccountUsers accountUsers = mapper.convertToEntity(accountUsersDto);

        // Assertions
        assertEquals(accountUsersDto.getAccountUsersId(), accountUsers.getAccountUsersId(), "AccountUsers IDs are not equals!");
        assertEquals(accountUsersDto.getUser2Id(), accountUsers.getUser2Id(), "AccountUsers User2ID are not equals!");
        assertEquals(accountUsersDto.getUser3Id(), accountUsers.getUser3Id(), "AccountUsers User3ID are not equals!");
        assertEquals(accountUsersDto.getUser4Id(), accountUsers.getUser4Id(), "AccountUsers User4ID are not equals!");
        assertEquals(accountUsersDto.getAccountId(), accountUsers.getAccount().getAccountId(), "Account IDs are not equals!");
    }

    /**
     * Test converting from null AccountUsersDto to AccountUsers
     */
    @Test
    void convertNullAccountUsersDtoToAccountUsers() {
        AccountUsersDto accountUsersDto = null;

        Mapper<AccountUsers, AccountUsersDto> mapper = new AccountUsersMapper();

        // Convert to entity
        AccountUsers accountUsers = mapper.convertToEntity(accountUsersDto);

        // Assertions
        assertNull(accountUsers, "AccountUsers is not null!");
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
        accountUsersDto.setUser2Id(2L);
        accountUsersDto.setUser3Id(3L);
        accountUsersDto.setUser4Id(4L);
        accountUsersDto.setAccountId(1L);

        return accountUsersDto;
    }
}