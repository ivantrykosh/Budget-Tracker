package com.ivantrykosh.app.budgettracker.server.services;

import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.Transaction;
import com.ivantrykosh.app.budgettracker.server.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test TransactionService
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({TransactionService.class, AccountService.class, UserService.class})
class TransactionServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    private User user;
    private Account account1;
    private Account account2;

    /**
     * Save user and accounts to db
     */
    @BeforeEach
    public void saveUsersAndAccount() {
        // First user
        User newUser = new User();
        newUser.setEmail("testemail@gmail.com");
        newUser.setPasswordHash("hash");
        newUser.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        newUser.setIsVerified(false);
        user = userService.saveUser(newUser);

        // Account 1
        Account newAccount1 = new Account();
        newAccount1.setName("accountTest1");
        newAccount1.setUser(user);
        account1 = accountService.saveAccount(newAccount1);

        // Account 2
        Account newAccount2 = new Account();
        newAccount2.setName("accountTest2");
        newAccount2.setUser(user);
        account2 = accountService.saveAccount(newAccount2);
    }

    /**
     * Test saving Transaction
     */
    @Test
    void saveTransaction() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        // Print account users and saved account users
        System.out.println(transaction + "\n" + savedTransaction);

        // Assert parameters are equals
        assertEquals(transaction.getAccount().getAccountId(), savedTransaction.getAccount().getAccountId(), "Accounts are not equals!");
        assertEquals(transaction.getValue(), savedTransaction.getValue(), "Transaction's values are not equals!");
    }

    /**
     * Test saving Transaction with invalid data
     */
    @Test
    void saveTransactionWihInvalidData() {
        Transaction transaction = createNewValidTransaction(100.0, null, account1);

        System.out.println(transaction);

        assertThrows(Exception.class, () -> transactionService.saveTransaction(transaction), "Exception was not thrown!");
    }

    /**
     * Test getting Transaction by ID
     */
    @Test
    void getTransactionById() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        // Get transaction by ID
        Transaction retrievedTransaction = transactionService.getTransactionById(savedTransaction.getTransactionId());

        // Print saved and retrieved account users
        System.out.println(savedTransaction + "\n" + retrievedTransaction);

        // Assert parameters are equals
        assertEquals(savedTransaction.getValue(), retrievedTransaction.getValue(), "Transaction's values are not equals!");
    }

    /**
     * Test getting Transaction by invalid ID
     */
    @Test
    void getTransactionByInvalidId() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        assertNull(transactionService.getTransactionById(Long.MAX_VALUE), "Transaction is not empty!");
    }

    /**
     * Test getting Transaction by AccountId
     */
    @Test
    void getTransactionsByAccountId() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Get transactions by AccountId
        List<Transaction> retrievedTransactions = transactionService.getTransactionsByAccountId(savedTransaction1.getAccount().getAccountId());

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(1, retrievedTransactions.size(), "Size of list is not 1!");
        assertEquals(savedTransaction1.getValue(), retrievedTransactions.get(0).getValue(), "Transaction's values are not equals!");
    }

    /**
     * Test getting Transaction by invalid AccountID
     */
    @Test
    void getTransactionsByInvalidAccountId() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        assertEquals(0, transactionService.getTransactionsByAccountId(Long.MAX_VALUE).size(), "List of transaction is not empty!");
    }

    /**
     * Test getting Transaction by AccountIDs
     */
    @Test
    void getTransactionsByAccountIds() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Get transactions by AccountIds
        List<Transaction> retrievedTransactions = transactionService.getTransactionsByAccountIds(List.of(account1.getAccountId(), account2.getAccountId()));

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(2, retrievedTransactions.size(), "Size of list is not 2!");
        assertEquals(savedTransaction1.getValue(), retrievedTransactions.get(1).getValue(), "Transaction's values are not equals!");
        assertEquals(savedTransaction2.getValue(), retrievedTransactions.get(0).getValue(), "Transaction's values are not equals!");
    }

    /**
     * Test getting Transaction by invalid AccountIDs
     */
    @Test
    void getTransactionsByInvalidAccountIds() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Get transactions by AccountIds
        List<Transaction> retrievedTransactions = transactionService.getTransactionsByAccountIds(List.of(Long.MAX_VALUE, Long.MAX_VALUE - 1));

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(0, retrievedTransactions.size(), "Size of list is not 0!");
    }

    /**
     * Test getting Transaction by AccountIDs and month
     */
    @Test
    void getTransactionsByAccountIdsAndMonth() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(2)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Get transactions by AccountIds and month
        List<Transaction> retrievedTransactions = transactionService.getTransactionsByAccountIdsAndMonth(List.of(account1.getAccountId(), account2.getAccountId()), Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(1, retrievedTransactions.size(), "Size of list is not 1!");
        assertEquals(savedTransaction1.getValue(), retrievedTransactions.get(0).getValue(), "Transaction's values are not equals!");
    }

    /**
     * Test getting Transaction by AccountIDs and invalid month
     */
    @Test
    void getTransactionsByAccountIdsAndInvalidMonth() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(2)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Get transactions by AccountIds and month
        List<Transaction> retrievedTransactions = transactionService.getTransactionsByAccountIdsAndMonth(List.of(account1.getAccountId(), account2.getAccountId()), Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(10)));

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(0, retrievedTransactions.size(), "Size of list is not 0!");
    }

    /**
     * Test getting income Transaction by AccountIDs
     */
    @Test
    void getIncomeTransactionsByAccountIds() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(2)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Get income transactions by AccountIds
        List<Transaction> retrievedTransactions = transactionService.getIncomeTransactionsByAccountIds(List.of(account1.getAccountId(), account2.getAccountId()), 0, 100);

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(1, retrievedTransactions.size(), "Size of list is not 1!");
        assertEquals(savedTransaction1.getValue(), retrievedTransactions.get(0).getValue(), "Transaction's values are not equals!");
    }

    /**
     * Test getting expense Transaction by AccountIDs
     */
    @Test
    void getExpenseTransactionsByAccountIds() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(2)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Get income transactions by AccountIds
        List<Transaction> retrievedTransactions = transactionService.getExpenseTransactionsByAccountIds(List.of(account1.getAccountId(), account2.getAccountId()), 0, 100);

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(1, retrievedTransactions.size(), "Size of list is not 1!");
        assertEquals(savedTransaction2.getValue(), retrievedTransactions.get(0).getValue(), "Transaction's values are not equals!");
    }

    /**
     * Test getting sum of income transactions by AccountID
     */
    @Test
    void getSumOfTransactionsWithAccountIdAndIncomeType() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(2)), account1);
        Transaction transaction3 = createNewValidTransaction(50.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(1)), account1);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);
        Transaction savedTransaction3 = transactionService.saveTransaction(transaction3);

        // Get sum of income transactions by AccountId
        Double sum = transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(account1.getAccountId(), true);

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + savedTransaction3 + "\n" + sum);

        // Assert parameters are equals
        assertEquals(Double.valueOf(150.0), sum, "Sum is not 150.0!");
    }

    /**
     * Test getting sum of expense transactions by AccountID
     */
    @Test
    void getSumOfTransactionsWithAccountIdAndExpenseType() {
        Transaction transaction1 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(2)), account1);
        Transaction transaction3 = createNewValidTransaction(-50.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMonths(1)), account1);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);
        Transaction savedTransaction3 = transactionService.saveTransaction(transaction3);

        // Get sum of income transactions by AccountId
        Double sum = transactionService.getSumOfTransactionsWithAccountIdAndSpecifiedType(account1.getAccountId(), false);

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + savedTransaction2 + "\n" + savedTransaction3 + "\n" + sum);

        // Assert parameters are equals
        assertEquals(Double.valueOf(-150.0), sum, "Sum is not -150.0!");
    }

    /**
     * Test updating Transaction
     */
    @Test
    void updateTransaction() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        // Update value
        transaction.setValue(120.0);

        // Update transaction
        Transaction updatedTransaction = transactionService.updateTransaction(transaction);

        // Print account and updatedAccount
        System.out.println(savedTransaction + "\n" + updatedTransaction);

        // Assert parameters are equals
        assertEquals(savedTransaction.getTransactionId(), updatedTransaction.getTransactionId(), "IDs are not equals!");
        assertEquals(savedTransaction.getValue(), updatedTransaction.getValue(), "Values are not equals!");
    }

    /**
     * Test deleting Transaction
     */
    @Test
    void deleteTransactionById() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        // Delete transaction
        Transaction deletedTransaction = transactionService.deleteTransactionById(savedTransaction.getTransactionId());

        // Print transaction and deleted transaction
        System.out.println(savedTransaction + "\n" + deletedTransaction);

        // Assert parameters are equals
        assertEquals(savedTransaction.getValue(), deletedTransaction.getValue(), "Values are not equals!");
        assertNull(transactionService.getTransactionById(savedTransaction.getTransactionId()), "Transaction is not deleted!");
    }

    /**
     * Test deleting not existing Transaction
     */
    @Test
    void deleteNotExistingTransactionById() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        assertNull(transactionService.getTransactionById(Long.MAX_VALUE), "Transaction is deleted!");
    }

    /**
     * Test deleting Transaction by AccountId
     */
    @Test
    void deleteTransactionsByAccountId() {
        Transaction transaction1 = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);
        Transaction transaction2 = createNewValidTransaction(-100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account2);

        // Save transactions
        Transaction savedTransaction1 = transactionService.saveTransaction(transaction1);
        Transaction savedTransaction2 = transactionService.saveTransaction(transaction2);

        // Delete transactions by AccountId
        List<Transaction> retrievedTransactions = transactionService.deleteTransactionsByAccountId(savedTransaction1.getAccount().getAccountId());

        // Print saved and retrieved account users
        System.out.println(savedTransaction1 + "\n" + retrievedTransactions);

        // Assert parameters are equals
        assertEquals(1, retrievedTransactions.size(), "Size of list is not 1!");
        assertEquals(savedTransaction1.getValue(), retrievedTransactions.get(0).getValue(), "Transaction's values are not equals!");
        assertEquals(0, transactionService.getTransactionsByAccountId(savedTransaction1.getAccount().getAccountId()).size(), "Transactions are not deleted!");
    }

    /**
     * Test deleting Transaction by invalid AccountID
     */
    @Test
    void deleteTransactionsByInvalidAccountId() {
        Transaction transaction = createNewValidTransaction(100.0, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)), account1);

        // Save transaction
        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        assertEquals(0, transactionService.deleteTransactionsByAccountId(Long.MAX_VALUE).size(), "List of transaction is not empty!");
    }

    /**
     * Create new valid Transaction
     * @param value value of transaction
     * @param date date of transaction
     * @param account account of transaction
     * @return new valid Transaction
     */
    private Transaction createNewValidTransaction(Double value, Date date, Account account) {
        Transaction newTransaction =  new Transaction();
        newTransaction.setCategory("testCategory");
        newTransaction.setValue(value);
        newTransaction.setDate(date);
        newTransaction.setToFromWhom("from mom");
        newTransaction.setNote("test note");
        newTransaction.setAccount(account);

        return newTransaction;
    }
}