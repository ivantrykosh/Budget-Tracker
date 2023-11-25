package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.AccountDto;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.services.TransactionService;

/**
 * Mapper for Account
 */
public class AccountMapper implements Mapper<Account, AccountDto> {

    private TransactionService transactionService;

    /**
     * Create an instance of AccountMapper with the specified TransactionService
     * @param transactionService TransactionService to be used by the AccountMapper.
     */
    public AccountMapper(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Convert from Account to AccountDto
     * @param account account to convert
     * @return AccountDto of account
     */
    @Override
    public AccountDto convertToDto(Account account) {
        if (account == null) {
            return null;
        }

        Long userId = null;
        if (account.getUser() != null) {
            userId = account.getUser().getUserId();
        }

        Double incomesSum = 0.0;
        Double expensesSum = 0.0;
        if (account.getAccountId() != null) {
            incomesSum = transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(account.getAccountId(), true);
            expensesSum = transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(account.getAccountId(), false);
        }

        AccountDto accountDto = new AccountDto();
        accountDto.setAccountId(account.getAccountId());
        accountDto.setName(account.getName());
        accountDto.setIncomesSum(incomesSum);
        accountDto.setExpensesSum(expensesSum);
        accountDto.setUserId(userId);
        return accountDto;
    }

    /**
     * Convert from AccountDto to Account
     * @param accountDto accountDto to convert
     * @return Account of accountDto
     */
    @Override
    public Account convertToEntity(AccountDto accountDto) {
        if (accountDto == null) {
            return null;
        }

        User user = null;
        if (accountDto.getUserId() != null) {
            user = new User();
            user.setUserId(accountDto.getUserId());
        }

        Account account = new Account();
        account.setAccountId(accountDto.getAccountId());
        account.setName(accountDto.getName());
        account.setUser(user);
        return account;
    }
}
