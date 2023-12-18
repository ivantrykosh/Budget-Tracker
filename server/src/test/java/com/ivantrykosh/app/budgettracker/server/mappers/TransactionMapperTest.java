package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.application.dtos.TransactionDto;
import com.ivantrykosh.app.budgettracker.server.application.mappers.Mapper;
import com.ivantrykosh.app.budgettracker.server.application.mappers.TransactionMapper;
import com.ivantrykosh.app.budgettracker.server.domain.model.Account;
import com.ivantrykosh.app.budgettracker.server.domain.model.Transaction;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test TransactionMapper
 */
class TransactionMapperTest {

    /**
     * Test converting from valid Transaction to TransactionDto
     */
    @Test
    void convertValidTransactionToTransactionDto() {
        Transaction transaction = createTransaction();

        Mapper<Transaction, TransactionDto> mapper = new TransactionMapper();

        // Convert to DTO
        TransactionDto transactionDto = mapper.convertToDto(transaction);

        // Assertions
        assertEquals(transaction.getTransactionId(), transactionDto.getTransactionId(), "Transaction IDs are not equals!");
        assertEquals(transaction.getCategory(), transactionDto.getCategory(), "Transaction categories are not equals!");
        assertEquals(transaction.getValue(), transactionDto.getValue(), "Transaction values are not equals!");
        assertEquals(transaction.getDate(), transactionDto.getDate(), "Transaction dates are not equals!");
        assertEquals(transaction.getToFromWhom(), transactionDto.getToFromWhom(), "Transaction ToFromWhoms are not equals!");
        assertEquals(transaction.getNote(), transactionDto.getNote(), "Transaction notes are not equals!");
        assertEquals(transaction.getAccount().getAccountId(), transactionDto.getAccountId(), "Account IDs are not equals!");
    }

    /**
     * Test converting from null Transaction to TransactionDto
     */
    @Test
    void convertNullTransactionToTransactionDto() {
        Transaction transaction = null;

        Mapper<Transaction, TransactionDto> mapper = new TransactionMapper();

        // Convert to DTO
        TransactionDto transactionDto = mapper.convertToDto(transaction);

        // Assertion
        assertNull(transactionDto, "TransactionDto is not null!");
    }

    /**
     * Test converting from valid TransactionDto to Transaction
     */
    @Test
    void convertValidTransactionDtoToTransaction() {
        TransactionDto transactionDto = createTransactionDto();

        Mapper<Transaction, TransactionDto> mapper = new TransactionMapper();

        // Convert to entity
        Transaction transaction = mapper.convertToEntity(transactionDto);

        // Assertions
        assertEquals(transactionDto.getTransactionId(), transaction.getTransactionId(), "Transaction IDs are not equals!");
        assertEquals(transactionDto.getCategory(), transaction.getCategory(), "Transaction categories are not equals!");
        assertEquals(transactionDto.getValue(), transaction.getValue(), "Transaction values are not equals!");
        assertEquals(transactionDto.getDate(), transaction.getDate(), "Transaction dates are not equals!");
        assertEquals(transactionDto.getToFromWhom(), transaction.getToFromWhom(), "Transaction ToFromWhoms are not equals!");
        assertEquals(transactionDto.getNote(), transaction.getNote(), "Transaction notes are not equals!");
        assertEquals(transactionDto.getAccountId(), transaction.getAccount().getAccountId(), "Account IDs are not equals!");
    }

    /**
     * Test converting from null TransactionDto to Transaction
     */
    @Test
    void convertNullTransactionDtoToTransaction() {
        TransactionDto transactionDto = null;

        Mapper<Transaction, TransactionDto> mapper = new TransactionMapper();

        // Convert to entity
        Transaction transaction = mapper.convertToEntity(transactionDto);

        // Assertion
        assertNull(transaction, "Transaction is not null!");
    }

    /**
     * Create new valid Transaction
     * @return new valid Transaction
     */
    private Transaction createTransaction() {
        // Account
        Account account = new Account();
        account.setAccountId(1L);

        // Transaction data
        Transaction transaction = new Transaction();
        transaction.setTransactionId(1L);
        transaction.setCategory("testCategory");
        transaction.setValue(100.0);
        transaction.setDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        transaction.setToFromWhom("testFromWhom");
        transaction.setNote("testNote");
        transaction.setAccount(account);

        return transaction;
    }

    /**
     * Create new valid TransactionDto
     * @return new valid TransactionDto
     */
    private TransactionDto createTransactionDto() {
        // TransactionDto data
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setTransactionId(1L);
        transactionDto.setCategory("testCategory");
        transactionDto.setValue(100.0);
        transactionDto.setDate(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        transactionDto.setToFromWhom("testFromWhom");
        transactionDto.setNote("testNote");
        transactionDto.setAccountId(1L);

        return transactionDto;
    }
}