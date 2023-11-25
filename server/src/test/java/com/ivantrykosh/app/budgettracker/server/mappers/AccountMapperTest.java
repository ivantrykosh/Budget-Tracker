package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.AccountDto;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.services.TransactionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test AccountMapper
 */
class AccountMapperTest {

    /**
     * Test converting from valid Account to AccountDto
     */
    @Test
    void convertValidAccountToAccountDto() {
        // Arrange
        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, true))
                .thenReturn(100.0);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, false))
                .thenReturn(-100.0);

        Account account = createAccount();

        Mapper<Account, AccountDto> mapper = new AccountMapper(transactionService);

        // Convert to DTO
        AccountDto accountDto = mapper.convertToDto(account);

        // Assertions
        assertEquals(account.getAccountId(), accountDto.getAccountId(), "Account IDs are not equals!");
        assertEquals(account.getName(), accountDto.getName(), "Account names are not equals!");
        assertEquals(account.getUser().getUserId(), accountDto.getUserId(), "User IDs are not equals!");
        assertEquals(Double.valueOf(100.0), accountDto.getIncomesSum(), "Incomes sum is not 100.0!");
        assertEquals(Double.valueOf(-100.0), accountDto.getExpensesSum(), "Expenses sum is not -100.0!");
    }

    /**
     * Test converting from null Account to AccountDto
     */
    @Test
    void convertNullAccountToAccountDto() {
        // Arrange
        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, true))
                .thenReturn(100.0);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, false))
                .thenReturn(-100.0);

        Account account = null;

        Mapper<Account, AccountDto> mapper = new AccountMapper(transactionService);

        // Convert to DTO
        AccountDto accountDto = mapper.convertToDto(account);

        // Assertion
        assertNull(accountDto, "AccountDto is not null!");
    }

    /**
     * Test converting from valid AccountDto to Account
     */
    @Test
    void convertValidAccountDtoToAccount() {
        // Arrange
        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, true))
                .thenReturn(100.0);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, false))
                .thenReturn(-100.0);


        AccountDto accountDto = createAccountDto();

        Mapper<Account, AccountDto> mapper = new AccountMapper(transactionService);

        // Convert to entity
        Account account = mapper.convertToEntity(accountDto);

        // Assertions
        assertEquals(accountDto.getAccountId(), account.getAccountId(), "Account IDs are not equals!");
        assertEquals(accountDto.getName(), account.getName(), "Account names are not equals!");
        assertEquals(accountDto.getUserId(), account.getUser().getUserId(), "User IDs are not equals!");
    }

    /**
     * Test converting from null AccountDto to Account
     */
    @Test
    void convertNullAccountDtoToAccount() {
        // Arrange
        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, true))
                .thenReturn(100.0);
        when(transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(1L, false))
                .thenReturn(-100.0);

        AccountDto accountDto = null;

        Mapper<Account, AccountDto> mapper = new AccountMapper(transactionService);

        // Convert to entity
        Account account = mapper.convertToEntity(accountDto);

        // Assertion
        assertNull(account, "AccountDto is not null!");
    }

    /**
     * Create new valid Account
     * @return new valid Account
     */
    private Account createAccount() {
        // User
        User user = new User();
        user.setUserId(1L);

        // Account data
        Account account = new Account();
        account.setAccountId(1L);
        account.setName("accountTest");
        account.setUser(user);

        return account;
    }

    /**
     * Create new valid AccountDto
     * @return new valid AccountDto
     */
    private AccountDto createAccountDto() {
        // AccountDto data
        AccountDto accountDto = new AccountDto();
        accountDto.setAccountId(1L);
        accountDto.setName("accountTest");
        accountDto.setUserId(1L);

        return accountDto;
    }
}