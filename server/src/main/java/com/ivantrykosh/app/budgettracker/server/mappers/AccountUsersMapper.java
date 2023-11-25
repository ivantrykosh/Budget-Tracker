package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.AccountUsersDto;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;

/**
 * Mapper for AccountUsers
 */
public class AccountUsersMapper implements Mapper<AccountUsers, AccountUsersDto> {

    /**
     * Convert from AccountUsers to AccountUsersDto
     * @param accountUsers accountUsers to convert
     * @return AccountUsersDto of accountUsers
     */
    @Override
    public AccountUsersDto convertToDto(AccountUsers accountUsers) {
        if (accountUsers == null) {
            return null;
        }

        Long accountId = null;
        if (accountUsers.getAccount() != null) {
            accountId = accountUsers.getAccount().getAccountId();
        }

        AccountUsersDto accountUsersDto = new AccountUsersDto();
        accountUsersDto.setAccountUsersId(accountUsers.getAccountUsersId());
        accountUsersDto.setUser2Id(accountUsers.getUser2Id());
        accountUsersDto.setUser3Id(accountUsers.getUser3Id());
        accountUsersDto.setUser4Id(accountUsers.getUser4Id());
        accountUsersDto.setAccountId(accountId);
        return accountUsersDto;
    }

    /**
     * Convert from AccountUsersDto to AccountUsers
     * @param accountUsersDto accountUsersDto to convert
     * @return AccountUsers of accountUsersDto
     */
    @Override
    public AccountUsers convertToEntity(AccountUsersDto accountUsersDto) {
        if (accountUsersDto == null) {
            return null;
        }

        Account account = null;
        if (accountUsersDto.getAccountId() != null) {
            account = new Account();
            account.setAccountId(accountUsersDto.getAccountId());
        }

        AccountUsers accountUsers = new AccountUsers();
        accountUsers.setAccountUsersId(accountUsersDto.getAccountUsersId());
        accountUsers.setUser2Id(accountUsersDto.getUser2Id());
        accountUsers.setUser3Id(accountUsersDto.getUser3Id());
        accountUsers.setUser4Id(accountUsersDto.getUser4Id());
        accountUsers.setAccount(account);
        return accountUsers;
    }
}
