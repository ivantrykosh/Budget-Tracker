package com.ivantrykosh.app.budgettracker.server.controllers;

import com.ivantrykosh.app.budgettracker.server.dtos.AccountDto;
import com.ivantrykosh.app.budgettracker.server.dtos.AccountUsersDto;
import com.ivantrykosh.app.budgettracker.server.mappers.AccountMapper;
import com.ivantrykosh.app.budgettracker.server.mappers.AccountUsersMapper;
import com.ivantrykosh.app.budgettracker.server.mappers.Mapper;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.requests.CreateAndChangeAccountRequest;
import com.ivantrykosh.app.budgettracker.server.responses.AccountResponse;
import com.ivantrykosh.app.budgettracker.server.services.AccountService;
import com.ivantrykosh.app.budgettracker.server.services.AccountUsersService;
import com.ivantrykosh.app.budgettracker.server.services.TransactionService;
import com.ivantrykosh.app.budgettracker.server.services.UserService;
import com.ivantrykosh.app.budgettracker.server.util.CustomUserDetails;
import com.ivantrykosh.app.budgettracker.server.validators.AccountValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Account REST controller
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountUsersService accountUsersService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private Mapper<Account, AccountDto> mapper = new AccountMapper(transactionService);
    @Autowired
    private Mapper<AccountUsers, AccountUsersDto> mapperAccountUsers = new AccountUsersMapper(userService);
    @Autowired
    private AccountValidator accountValidator;
    Logger logger = LoggerFactory.getLogger(AccountController.class); // Logger

    /**
     * Endpoint to create a new account for the currently authenticated user and associate it with additional users.
     *
     * @param createAndChangeAccountRequest The request object containing account details and additional user emails.
     * @return ResponseEntity with the result of the account creation process and HttpStatus indicating the result.
     */
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createAccount(@RequestBody CreateAndChangeAccountRequest createAndChangeAccountRequest) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        List<Account> accounts = accountService.getAccountsByUserId(user.getUserId());
        if (!accountValidator.checkName(createAndChangeAccountRequest.getName(), accounts)) {
            logger.error("Invalid name " + createAndChangeAccountRequest.getName() + " of account for user email " + user.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid name of account!");
        }

        Account account = new Account();
        account.setName(createAndChangeAccountRequest.getName());
        account.setUser(user);
        Account savedAccount = accountService.saveAccount(account);

        logger.info("Account with name " + account.getName() + " of user " + user.getEmail() + " was saved");

        AccountUsers accountUsers = new AccountUsers();
        accountUsers.setAccount(savedAccount);
        AccountUsers savedAccountUsers = accountUsersService.saveAccountUsers(accountUsers);

        logger.info("AccountUsers with ID " + account.getAccountId() + " of user " + user.getEmail() + " was saved");

        if (createAndChangeAccountRequest.getEmail2() != null) {
            if (!accountValidator.checkEmail(createAndChangeAccountRequest.getEmail2(), account)
                    || createAndChangeAccountRequest.getEmail2().equals(createAndChangeAccountRequest.getEmail3())
                    || createAndChangeAccountRequest.getEmail2().equals(createAndChangeAccountRequest.getEmail4())) {
                logger.error("Invalid email2 " + createAndChangeAccountRequest.getEmail2() + " for AccountUsers with ID " + accountUsers.getAccountUsersId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email " + createAndChangeAccountRequest.getEmail2() + "!");
            }
        }
        if (createAndChangeAccountRequest.getEmail3() != null) {
            if (!accountValidator.checkEmail(createAndChangeAccountRequest.getEmail3(), account)
                    || createAndChangeAccountRequest.getEmail3().equals(createAndChangeAccountRequest.getEmail2())
                    || createAndChangeAccountRequest.getEmail3().equals(createAndChangeAccountRequest.getEmail4())) {
                logger.error("Invalid email3 " + createAndChangeAccountRequest.getEmail3() + " for AccountUsers with ID " + accountUsers.getAccountUsersId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email " + createAndChangeAccountRequest.getEmail3() + "!");
            }
        }
        if (createAndChangeAccountRequest.getEmail4() != null) {
            if (!accountValidator.checkEmail(createAndChangeAccountRequest.getEmail4(), account)
                    || createAndChangeAccountRequest.getEmail4().equals(createAndChangeAccountRequest.getEmail2())
                    || createAndChangeAccountRequest.getEmail4().equals(createAndChangeAccountRequest.getEmail3())) {
                logger.error("Invalid email4 " + createAndChangeAccountRequest.getEmail4() + " for AccountUsers with ID " + accountUsers.getAccountUsersId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email " + createAndChangeAccountRequest.getEmail4() + "!");
            }
        }

        if (createAndChangeAccountRequest.getEmail2() != null) {
            savedAccountUsers.setUser2Id(
                    userService.getUserByEmail(createAndChangeAccountRequest.getEmail2()).getUserId()
            );
        }
        if (createAndChangeAccountRequest.getEmail3() != null) {
            savedAccountUsers.setUser3Id(
                    userService.getUserByEmail(createAndChangeAccountRequest.getEmail3()).getUserId()
            );
        }
        if (createAndChangeAccountRequest.getEmail4() != null) {
            savedAccountUsers.setUser4Id(
                    userService.getUserByEmail(createAndChangeAccountRequest.getEmail4()).getUserId()
            );
        }

        AccountUsers updatedAccountUsers = accountUsersService.updateAccountUsers(savedAccountUsers);

        logger.info("AccountUsers with ID " + account.getAccountId() + " of user " + user.getEmail() + " was updated");

        return ResponseEntity.status(HttpStatus.OK).body(
                new AccountResponse(
                        mapper.convertToDto(account),
                        mapperAccountUsers.convertToDto(updatedAccountUsers)
                )
        );
    }

    /**
     * Endpoint to retrieve details of an account based on the provided account ID.
     *
     * @param id The ID of the account to retrieve.
     * @return ResponseEntity with the result of the account retrieval process and HttpStatus indicating the result.
     */
    @GetMapping("/get")
    public ResponseEntity<?> getAccount(@RequestParam String id) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        long accountId;
        try {
            accountId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            logger.error("Invalid ID " + id + " of account");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of account!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            logger.error("No account with ID " + accountId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                logger.error("User with email " + user.getEmail() + " does not have permission to get account with ID " + account.getAccountId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
            }
        }

        AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());

        logger.info("Account with ID " + account.getAccountId() + " and AccountUsers with ID " + accountUsers.getAccountUsersId() + " were got successfully");

        return ResponseEntity.status(HttpStatus.OK).body(
                new AccountResponse(
                        mapper.convertToDto(account),
                        mapperAccountUsers.convertToDto(accountUsers)
                )
        );
    }

    /**
     * Endpoint to retrieve details of all accounts associated with the currently authenticated user.
     *
     * @return ResponseEntity with the result of the accounts retrieval process and HttpStatus indicating the result.
     */
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllAccounts() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        List<Account> accounts = accountService.getAccountsByUserId(user.getUserId());
        List<AccountUsers> accountsUsers = accountUsersService.getAccountsUsersByUserId(user.getUserId());

        for (AccountUsers accountUsers : accountsUsers) {
            accounts.add(
                    accountService.getAccountById(accountUsers.getAccount().getAccountId())
            );
        }

        logger.info("All Accounts and AccountUsers for user with email " + user.getEmail() + " were got successfully");

        return ResponseEntity.status(HttpStatus.OK).body(
                accounts.stream()
                        .map(account -> mapper.convertToDto(account))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Endpoint to update details of an account based on the provided account ID.
     *
     * @param id The ID of the account to update.
     * @param createAndChangeAccountRequest The request object containing updated account details and additional user emails.
     * @return ResponseEntity with the result of the account update process and HttpStatus indicating the result.
     */
    @PatchMapping("/update")
    @Transactional
    public ResponseEntity<?> updateAccount(@RequestParam String id, @RequestBody CreateAndChangeAccountRequest createAndChangeAccountRequest) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        long accountId;
        try {
            accountId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            logger.error("Invalid ID " + id + " of account");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of account!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            logger.error("No account with ID " + accountId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            logger.error("User with email " + user.getEmail() + " does not have permission to get account with ID " + account.getAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
        }

        List<Account> accounts = accountService.getAccountsByUserId(user.getUserId());
        if (!account.getName().equals(createAndChangeAccountRequest.getName()) && !accountValidator.checkName(createAndChangeAccountRequest.getName(), accounts)) {
            logger.error("Invalid name " + createAndChangeAccountRequest.getName() + " of account of user with email " + user.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid name of account!");
        }

        account.setName(createAndChangeAccountRequest.getName());

        AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
        if (createAndChangeAccountRequest.getEmail2() != null) {
            if (!accountValidator.checkEmail(createAndChangeAccountRequest.getEmail2(), account)
                    || createAndChangeAccountRequest.getEmail2().equals(createAndChangeAccountRequest.getEmail3())
                    || createAndChangeAccountRequest.getEmail2().equals(createAndChangeAccountRequest.getEmail4())) {
                logger.error("Invalid email2 " + createAndChangeAccountRequest.getEmail2() + " for AccountUsers with ID " + accountUsers.getAccountUsersId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email " + createAndChangeAccountRequest.getEmail2() + "!");
            }
        }
        if (createAndChangeAccountRequest.getEmail3() != null) {
            if (!accountValidator.checkEmail(createAndChangeAccountRequest.getEmail3(), account)
                    || createAndChangeAccountRequest.getEmail3().equals(createAndChangeAccountRequest.getEmail2())
                    || createAndChangeAccountRequest.getEmail3().equals(createAndChangeAccountRequest.getEmail4())) {
                logger.error("Invalid email3 " + createAndChangeAccountRequest.getEmail3() + " for AccountUsers with ID " + accountUsers.getAccountUsersId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email " + createAndChangeAccountRequest.getEmail3() + "!");
            }
        }
        if (createAndChangeAccountRequest.getEmail4() != null) {
            if (!accountValidator.checkEmail(createAndChangeAccountRequest.getEmail4(), account)
                    || createAndChangeAccountRequest.getEmail4().equals(createAndChangeAccountRequest.getEmail2())
                    || createAndChangeAccountRequest.getEmail4().equals(createAndChangeAccountRequest.getEmail3())) {
                logger.error("Invalid email4 " + createAndChangeAccountRequest.getEmail4() + " for AccountUsers with ID " + accountUsers.getAccountUsersId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email " + createAndChangeAccountRequest.getEmail4() + "!");
            }
        }

        if (createAndChangeAccountRequest.getEmail2() != null) {
            accountUsers.setUser2Id(
                    userService.getUserByEmail(createAndChangeAccountRequest.getEmail2()).getUserId()
            );
        } else {
            accountUsers.setUser2Id(null);
        }
        if (createAndChangeAccountRequest.getEmail3() != null) {
            accountUsers.setUser3Id(
                    userService.getUserByEmail(createAndChangeAccountRequest.getEmail3()).getUserId()
            );
        } else {
            accountUsers.setUser3Id(null);
        }
        if (createAndChangeAccountRequest.getEmail4() != null) {
            accountUsers.setUser4Id(
                    userService.getUserByEmail(createAndChangeAccountRequest.getEmail4()).getUserId()
            );
        } else {
            accountUsers.setUser4Id(null);
        }

        Account updatedAccount = accountService.updateAccount(account);
        logger.info("Account with ID " + updatedAccount.getAccountId() + " was updated");

        AccountUsers updatedAccountUsers = accountUsersService.updateAccountUsers(accountUsers);
        logger.info("AccountUsers with ID " + updatedAccountUsers.getAccountUsersId() + " was updated");

        return ResponseEntity.status(HttpStatus.OK).body(
                new AccountResponse(
                        mapper.convertToDto(updatedAccount),
                        mapperAccountUsers.convertToDto(updatedAccountUsers)
                )
        );
    }

    /**
     * Endpoint to delete an account based on the provided account ID.
     *
     * @param id The ID of the account to delete.
     * @return ResponseEntity with a success message or an error message and HttpStatus indicating the result.
     */
    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<String> deleteAccount(@RequestParam String id) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        long accountId;
        try {
            accountId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            logger.error("Invalid ID " + id + " of account");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of account!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            logger.error("No account with ID " + accountId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            logger.error("User with email " + user.getEmail() + " does not have permission to get account with ID " + account.getAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
        }
        AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());

        transactionService.deleteTransactionsByAccountId(account.getAccountId());
        logger.info("Transactions of user " + user.getEmail() + " and account with ID " + account.getAccountId() + " were deleted");

        accountUsersService.deleteAccountUsersById(accountUsers.getAccountUsersId());
        logger.info("AccountUsers of user " + user.getEmail() + " with ID " + accountUsers.getAccountUsersId() + " was deleted");

        accountService.deleteAccountById(account.getAccountId());
        logger.info("Account of user " + user.getEmail() + " with ID " + account.getAccountId() + " was deleted");

        return ResponseEntity.status(HttpStatus.OK).body("Account was deleted!");
    }
}
