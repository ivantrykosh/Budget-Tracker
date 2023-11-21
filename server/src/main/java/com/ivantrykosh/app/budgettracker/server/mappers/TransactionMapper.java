package com.ivantrykosh.app.budgettracker.server.mappers;

import com.ivantrykosh.app.budgettracker.server.dtos.TransactionDto;
import com.ivantrykosh.app.budgettracker.server.model.Account;
import com.ivantrykosh.app.budgettracker.server.model.Transaction;

/**
 * Mapper for Transaction
 */
public class TransactionMapper implements Mapper<Transaction, TransactionDto> {

    /**
     * Convert from Transaction to TransactionDto
     * @param transaction transaction to convert
     * @return TransactionDto of transaction
     */
    @Override
    public TransactionDto convertToDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        Long accountId = null;
        if (transaction.getAccount() != null) {
            accountId = transaction.getAccount().getAccountId();
        }

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setTransactionId(transaction.getTransactionId());
        transactionDto.setCategory(transaction.getCategory());
        transactionDto.setValue(transaction.getValue());
        transactionDto.setDate(transaction.getDate());
        transactionDto.setToFromWhom(transaction.getToFromWhom());
        transactionDto.setNote(transaction.getNote());
        transactionDto.setAccountId(accountId);
        return transactionDto;
    }

    /**
     * Convert from TransactionDto to Transaction
     * @param transactionDto transactionDto to convert
     * @return Transaction of transactionDto
     */
    @Override
    public Transaction convertToEntity(TransactionDto transactionDto) {
        if (transactionDto == null) {
            return null;
        }

        Account account = null;
        if (transactionDto.getAccountId() != null) {
            account = new Account();
            account.setAccountId(transactionDto.getAccountId());
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionDto.getTransactionId());
        transaction.setCategory(transactionDto.getCategory());
        transaction.setValue(transactionDto.getValue());
        transaction.setDate(transactionDto.getDate());
        transaction.setToFromWhom(transactionDto.getToFromWhom());
        transaction.setNote(transactionDto.getNote());
        transaction.setAccount(account);
        return transaction;
    }
}
