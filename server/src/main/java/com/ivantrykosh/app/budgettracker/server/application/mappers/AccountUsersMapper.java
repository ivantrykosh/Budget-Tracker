package com.ivantrykosh.app.budgettracker.server.application.mappers;

import com.ivantrykosh.app.budgettracker.server.application.dtos.AccountUsersDto;
import com.ivantrykosh.app.budgettracker.server.domain.model.Account;
import com.ivantrykosh.app.budgettracker.server.domain.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import com.ivantrykosh.app.budgettracker.server.application.services.UserService;
import org.springframework.stereotype.Component;

/**
 * Mapper for AccountUsers
 */
@Component
public class AccountUsersMapper implements Mapper<AccountUsers, AccountUsersDto> {

    private UserService userService;

    /**
     * Create an instance of AccountUsersMapper with the specified UserService
     * @param userService UserService to be used by the AccountMapper.
     */
    public AccountUsersMapper(UserService userService) {
        this.userService = userService;
    }

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
        User user = accountUsers.getUser2Id() != null ? userService.getUserById(accountUsers.getUser2Id()) : null;
        if (user == null) {
            accountUsersDto.setEmail2(null);
        } else {
            accountUsersDto.setEmail2(user.getEmail());
        }
        user = accountUsers.getUser3Id() != null ? userService.getUserById(accountUsers.getUser3Id()) : null;
        if (user == null) {
            accountUsersDto.setEmail3(null);
        } else {
            accountUsersDto.setEmail3(user.getEmail());
        }
        user = accountUsers.getUser4Id() != null ? userService.getUserById(accountUsers.getUser4Id()) : null;
        if (user == null) {
            accountUsersDto.setEmail4(null);
        } else {
            accountUsersDto.setEmail4(user.getEmail());
        }
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

        User user = userService.getUserByEmail(accountUsersDto.getEmail2());
        if (user == null) {
            accountUsers.setUser2Id(null);
        } else {
            accountUsers.setUser2Id(user.getUserId());
        }
        user = userService.getUserByEmail(accountUsersDto.getEmail3());
        if (user == null) {
            accountUsers.setUser3Id(null);
        } else {
            accountUsers.setUser3Id(user.getUserId());
        }
        user = userService.getUserByEmail(accountUsersDto.getEmail4());
        if (user == null) {
            accountUsers.setUser4Id(null);
        } else {
            accountUsers.setUser4Id(user.getUserId());
        }
        accountUsers.setAccount(account);
        return accountUsers;
    }
}
