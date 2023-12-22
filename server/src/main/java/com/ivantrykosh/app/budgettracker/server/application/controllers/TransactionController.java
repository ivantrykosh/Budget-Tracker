package com.ivantrykosh.app.budgettracker.server.application.controllers;

import com.ivantrykosh.app.budgettracker.server.application.dtos.TransactionDto;
import com.ivantrykosh.app.budgettracker.server.application.mappers.Mapper;
import com.ivantrykosh.app.budgettracker.server.application.mappers.TransactionMapper;
import com.ivantrykosh.app.budgettracker.server.domain.model.Account;
import com.ivantrykosh.app.budgettracker.server.domain.model.AccountUsers;
import com.ivantrykosh.app.budgettracker.server.domain.model.Transaction;
import com.ivantrykosh.app.budgettracker.server.domain.model.User;
import com.ivantrykosh.app.budgettracker.server.application.services.AccountService;
import com.ivantrykosh.app.budgettracker.server.application.services.AccountUsersService;
import com.ivantrykosh.app.budgettracker.server.application.services.TransactionService;
import com.ivantrykosh.app.budgettracker.server.application.services.UserService;
import com.ivantrykosh.app.budgettracker.server.util.CustomUserDetails;
import com.ivantrykosh.app.budgettracker.server.validators.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
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
    Logger logger = LoggerFactory.getLogger(TransactionController.class); // Logger

    /**
     * Endpoint to create a new transaction based on the provided TransactionDto.
     *
     * @param transactionDto The TransactionDto containing the information for the new transaction.
     * @return ResponseEntity with a success message or an error message and HttpStatus indicating the result.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionDto transactionDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        if (!transactionValidator.checkAccountId(transactionDto.getAccountId())) {
            logger.error("Invalid account ID: " + transactionDto.getAccountId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountId!");
        }
        if (!transactionValidator.checkCategory(transactionDto.getCategory())) {
            logger.error("Invalid category: " + transactionDto.getCategory());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category!");
        }
        if (!transactionValidator.checkValue(transactionDto.getValue())) {
            logger.error("Invalid value: " + transactionDto.getValue());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid value!");
        }
        if (!transactionValidator.checkDate(transactionDto.getDate())) {
            logger.error("Invalid date: " + transactionDto.getDate());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date!");
        }
        if (!transactionValidator.checkToFromWhom(transactionDto.getToFromWhom())) {
            logger.error("Invalid toFromWhom: " + transactionDto.getToFromWhom());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid toFromWhom!");
        }
        if (!transactionValidator.checkNote(transactionDto.getNote())) {
            logger.error("Invalid note: " + transactionDto.getNote());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid note!");
        }

        Account account = accountService.getAccountById(transactionDto.getAccountId());
        if (account == null) {
            logger.error("No account with ID " + transactionDto.getAccountId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                logger.error("User with email " + user.getEmail() + " does not have permission to account with ID " + account.getAccountId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to account with name + " + account.getName() + "!");
            }
        }

        transactionDto.setTransactionId(null);
        Transaction savedTransaction = transactionService.saveTransaction(
                mapper.convertToEntity(transactionDto)
        );

        logger.info("Transaction was saved with ID " + savedTransaction);

        return ResponseEntity.status(HttpStatus.CREATED).body("Transaction was saved!");
    }

    /**
     * Endpoint to retrieve a transaction based on the provided transaction ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return ResponseEntity with a TransactionDto object or an error message and HttpStatus indicating the result.
     */
    @GetMapping("/get")
    public ResponseEntity<?> getTransactionById(@RequestParam String id) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        long transactionId;
        try {
            transactionId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            logger.error("Invalid ID " + id + " of transaction");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of transaction!");
        }

        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            logger.error("No transaction with ID " + transactionId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No transaction with given id!");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Account account = accountService.getAccountById(transaction.getAccount().getAccountId());
        if (account == null) {
            logger.error("No account with ID " + transaction.getAccount().getAccountId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                logger.error("User with email " + user.getEmail() + " does not have permission to account with ID " + account.getAccountId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to account with name + " + account.getName() + "!");
            }
        }

        logger.info("Transaction with ID " + transaction.getTransactionId() + " was got");

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
                logger.error("User with email " + user.getEmail() + " does not have permission to account with ID " + account.getAccountId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to account with name + " + account.getName() + "!");
            }
        }

        List<Transaction> transactions = transactionService.getTransactionsByAccountId(account.getAccountId());

        logger.info("All transactions of account with ID " + account.getAccountId() + " were got");

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
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        if (accountIds == null || accountIds.isEmpty()) {
            logger.error("Invalid accountIDs " + accountIds);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
        }
        for (Long accountId : accountIds) {
            if (accountId == null) {
                logger.error("Invalid accountIDs " + accountIds);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
            }
        }

        accountIds = accountIds.stream().distinct().collect(Collectors.toList());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        for (Long accountId : accountIds) {
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
                    logger.error("User with email " + user.getEmail() + " does not have permission to account with ID " + account.getAccountId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to account with name + " + account.getName() + "!");
                }
            }
        }

        List<Transaction> transactions = transactionService.getTransactionsByAccountIds(accountIds);

        logger.info("All transaction for account IDs " + accountIds + " were got");

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
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        if (accountIds == null || accountIds.isEmpty()) {
            logger.error("Invalid accountIDs " + accountIds);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
        }
        for (Long accountId : accountIds) {
            if (accountId == null) {
                logger.error("Invalid accountIDs " + accountIds);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountIds!");
            }
        }
        if (startDate == null) {
            logger.error("Invalid startDate");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid startDate!");
        }
        if (endDate == null) {
            logger.error("Invalid endDate");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid endDate!");
        }
        if (startDate.after(endDate)) {
            logger.error("startDate is after endDate");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid startDate. It can't be after endDate!");
        }

        accountIds = accountIds.stream().distinct().collect(Collectors.toList());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        for (Long accountId : accountIds) {
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
                    logger.error("User with email " + user.getEmail() + " does not have permission to account with ID " + account.getAccountId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to account with name + " + account.getName() + "!");
                }
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        List<Transaction> transactions = transactionService.getTransactionsByAccountIdsAndDateBetween(accountIds, startDate, calendar.getTime());
        logger.info("All transactions for accountIDs " + accountIds + " and between dates " + startDate + " and " + endDate);

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
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        if (!transactionValidator.checkTransactionId(transactionDto.getTransactionId())) {
            logger.error("Invalid transaction ID: " + transactionDto.getTransactionId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid transactionId!");
        }
        if (!transactionValidator.checkAccountId(transactionDto.getAccountId())) {
            logger.error("Invalid account ID: " + transactionDto.getAccountId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid accountId!");
        }
        if (!transactionValidator.checkCategory(transactionDto.getCategory())) {
            logger.error("Invalid category: " + transactionDto.getCategory());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category!");
        }
        if (!transactionValidator.checkValue(transactionDto.getValue())) {
            logger.error("Invalid value: " + transactionDto.getValue());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid value!");
        }
        if (!transactionValidator.checkDate(transactionDto.getDate())) {
            logger.error("Invalid date: " + transactionDto.getDate());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date!");
        }
        if (!transactionValidator.checkToFromWhom(transactionDto.getToFromWhom())) {
            logger.error("Invalid toFromWhom: " + transactionDto.getToFromWhom());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid toFromWhom!");
        }
        if (!transactionValidator.checkNote(transactionDto.getNote())) {
            logger.error("Invalid note: " + transactionDto.getNote());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid note!");
        }

        Account account = accountService.getAccountById(transactionDto.getAccountId());
        if (account == null) {
            logger.error("No account with ID " + transactionDto.getTransactionId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                logger.error("User with email " + user.getEmail() + " does not have permission to account with ID " + account.getAccountId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to account with name + " + account.getName() + "!");
            }
        }

        transactionService.updateTransaction(
                mapper.convertToEntity(transactionDto)
        );

        logger.info("Transaction with ID " + transactionDto.getTransactionId() + " was updated");

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
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!customUserDetails.isEnabled()) {
            logger.error("Email " + customUserDetails.getUsername() + " is not verified");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not verified!");
        }

        long transactionId;
        try {
            transactionId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            logger.error("Invalid ID " + id + " of transaction");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id of transaction!");
        }

        if (!transactionValidator.checkTransactionId(transactionId)) {
            logger.error("Invalid transaction ID " + transactionId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid transactionId!");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        Transaction transaction = transactionService.getTransactionById(transactionId);

        Account account = accountService.getAccountById(transaction.getAccount().getAccountId());
        if (account == null) {
            logger.error("No account with ID " + transaction.getAccount().getAccountId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No account with given id!");
        }
        if (account.getUser().getUserId() != user.getUserId()) {
            AccountUsers accountUsers = accountUsersService.getAccountUsersByAccountId(account.getAccountId());
            if (accountUsers.getUser2Id() != user.getUserId()
                    && accountUsers.getUser3Id() != user.getUserId()
                    && accountUsers.getUser4Id() != user.getUserId()) {
                logger.error("User with email " + user.getEmail() + " does not have permission to account with ID " + account.getAccountId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to account with name + " + account.getName() + "!");
            }
        }

        transactionService.deleteTransactionById(transaction.getTransactionId());

        logger.info("Transaction with ID " + transactionId + " was deleted");

        return ResponseEntity.status(HttpStatus.OK).body("Transaction was deleted!");
    }
}
