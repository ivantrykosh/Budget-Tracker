package com.ivantrykosh.app.budgettracker.server.controllers;

import com.ivantrykosh.app.budgettracker.server.dtos.TransactionDto;
import com.ivantrykosh.app.budgettracker.server.mappers.Mapper;
import com.ivantrykosh.app.budgettracker.server.mappers.TransactionMapper;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.model.Transaction;
import com.ivantrykosh.app.budgettracker.server.model.User;
import com.ivantrykosh.app.budgettracker.server.services.AccountService;
import com.ivantrykosh.app.budgettracker.server.services.AccountUsersService;
import com.ivantrykosh.app.budgettracker.server.services.TransactionService;
import com.ivantrykosh.app.budgettracker.server.services.UserService;
import com.ivantrykosh.app.budgettracker.server.validators.TransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction REST controller
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountUsersService accountUsersService;
    @Autowired
    private TransactionService transactionService;
    private Mapper<Transaction, TransactionDto> mapper = new TransactionMapper();
    @Autowired
    private TransactionValidator transactionValidator;

    /**
     * Endpoint to create a new transaction based on the provided TransactionDto.
     *
     * @param transactionDto The TransactionDto containing the information for the new transaction.
     * @return ResponseEntity with a success message or an error message and HttpStatus indicating the result.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionDto transactionDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        if (!transactionValidator.checkAccountId(transactionDto.getAccountId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountId!");
        }
        if (!transactionValidator.checkCategory(transactionDto.getCategory())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category!");
        }
        if (!transactionValidator.checkValue(transactionDto.getValue())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid value!");
        }
        if (!transactionValidator.checkDate(transactionDto.getDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date!");
        }
        if (!transactionValidator.checkToFromWhom(transactionDto.getToFromWhom())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid toFromWhom!");
        }
        if (!transactionValidator.checkNote(transactionDto.getNote())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid note!");
        }

        Account account = accountService.getAccountById(transactionDto.getAccountId());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
            }
        }

        transactionDto.setTransactionId(null);
        transactionService.saveTransaction(
                mapper.convertToEntity(transactionDto)
        );

        return ResponseEntity.status(HttpStatus.OK).body("Transaction was saved!");
    }

    /**
     * Endpoint to retrieve a transaction based on the provided transaction ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return ResponseEntity with a TransactionDto object or an error message and HttpStatus indicating the result.
     */
    @GetMapping("/get")
    public ResponseEntity<?> getTransactionById(@RequestParam String id) {
        long transactionId;
        try {
            transactionId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of transaction!");
        }

        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No transaction with given id!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Account account = accountService.getAccountById(transaction.getAccount().getAccountId());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                mapper.convertToDto(transaction)
        );
    }

    /**
     * Endpoint to retrieve transactions for a specific account based on the provided account ID.
     *
     * @param id The ID of the account for which transactions are requested.
     * @return ResponseEntity with a list of TransactionDto objects or an error message and HttpStatus indicating the result.
     */
    @GetMapping("/get-all-by-account")
    public ResponseEntity<?> getTransactionsByAccountId(@RequestParam String id) {
        long accountId;
        try {
            accountId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of account!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
            }
        }

        List<Transaction> transactions = transactionService.getTransactionsByAccountId(account.getAccountId());

        return ResponseEntity.status(HttpStatus.OK).body(
                transactions.stream()
                        .map(transaction -> mapper.convertToDto(transaction))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Endpoint to retrieve transactions for multiple account IDs.
     *
     * @param accountIds The list of account IDs.
     * @return ResponseEntity with a list of TransactionDto objects or an error message and HttpStatus indicating the result.
     */
    @GetMapping("/get-all")
    public ResponseEntity<?> getTransactionByAllAccountIds(@RequestParam List<Long> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
        }
        for (Long accountId : accountIds) {
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
            }
        }

        accountIds = accountIds.stream().distinct().collect(Collectors.toList());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        for (Long accountId : accountIds) {
            Account account = accountService.getAccountById(accountId);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
            }
            if (account.getUser().getUserId() != user.getUserId()) {
                AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
                if (accountUsers.getUser2Id() != user.getUserId()
                        && accountUsers.getUser3Id() != user.getUserId()
                        && accountUsers.getUser4Id() != user.getUserId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
                }
            }
        }

        List<Transaction> transactions = transactionService.getTransactionsByAccountIds(accountIds);

        return ResponseEntity.status(HttpStatus.OK).body(
                transactions.stream()
                        .map(transaction -> mapper.convertToDto(transaction))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Endpoint to retrieve transactions for multiple account IDs within a specified date range.
     *
     * @param accountIds The list of account IDs.
     * @param startDate The start date.
     * @param endDate The end date.
     * @return ResponseEntity with a list of TransactionDto objects or an error message and HttpStatus indicating the result.
     */
    @GetMapping("/get-all-between-dates")
    public ResponseEntity<?> getTransactionByAllAccountIdsAndDateBetween(@RequestParam List<Long> accountIds,
                                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        if (accountIds == null || accountIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
        }
        for (Long accountId : accountIds) {
            if (accountId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
            }
        }
        if (startDate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid startDate!");
        }
        if (endDate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid endDate!");
        }
        if (startDate.after(endDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid startDate. It can't be after endDate!");
        }

        accountIds = accountIds.stream().distinct().collect(Collectors.toList());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        for (Long accountId : accountIds) {
            Account account = accountService.getAccountById(accountId);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
            }
            if (account.getUser().getUserId() != user.getUserId()) {
                AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
                if (accountUsers.getUser2Id() != user.getUserId()
                        && accountUsers.getUser3Id() != user.getUserId()
                        && accountUsers.getUser4Id() != user.getUserId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
                }
            }
        }

        List<Transaction> transactions = transactionService.getTransactionsByAccountIdsAndDateBetween(accountIds, startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(
                transactions.stream()
                        .map(transaction -> mapper.convertToDto(transaction))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Endpoint to update a transaction based on the provided TransactionDto.
     *
     * @param transactionDto The TransactionDto containing the updated transaction information.
     * @return ResponseEntity with a success message or an error message and HttpStatus indicating the result.
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateTransaction(@RequestBody TransactionDto transactionDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        if (!transactionValidator.checkTransactionId(transactionDto.getTransactionId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid transactionId!");
        }
        if (!transactionValidator.checkAccountId(transactionDto.getAccountId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountId!");
        }
        if (!transactionValidator.checkCategory(transactionDto.getCategory())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category!");
        }
        if (!transactionValidator.checkValue(transactionDto.getValue())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid value!");
        }
        if (!transactionValidator.checkDate(transactionDto.getDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date!");
        }
        if (!transactionValidator.checkToFromWhom(transactionDto.getToFromWhom())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid toFromWhom!");
        }
        if (!transactionValidator.checkNote(transactionDto.getNote())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid note!");
        }

        Account account = accountService.getAccountById(transactionDto.getAccountId());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
            }
        }

        transactionService.updateTransaction(
                mapper.convertToEntity(transactionDto)
        );

        return ResponseEntity.status(HttpStatus.OK).body("Transaction was updated!");
    }

    /**
     * Endpoint to delete a transaction based on the provided transaction ID.
     *
     * @param id The ID of the transaction to delete.
     * @return ResponseEntity with a success message or an error message and HttpStatus indicating the result.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTransaction(@RequestParam String id) {
        long transactionId;
        try {
            transactionId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of transaction!");
        }

        if (!transactionValidator.checkTransactionId(transactionId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid transactionId!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Transaction transaction = transactionService.getTransactionById(transactionId);

        Account account = accountService.getAccountById(transaction.getAccount().getAccountId());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission!");
            }
        }

        transactionService.deleteTransactionById(transaction.getTransactionId());

        return ResponseEntity.status(HttpStatus.OK).body("Transaction was deleted!");
    }
}
