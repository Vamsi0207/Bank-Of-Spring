package com.banking.demo.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.banking.demo.Models.Account;
import com.banking.demo.Models.Transaction;
import com.banking.demo.Repo.AccountRepo;
import com.banking.demo.Repo.TransactionRepo;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepo trepo;

    @Autowired
    private AccountRepo arepo;

    @Autowired
    private AccountService aser;

    /**
     * Handles money transfer between two accounts.
     * Deducts from sender and adds to receiver. Also creates two transaction records.
     * Throws exception if insufficient funds or invalid accounts.
     */
    @Transactional
    public void transact(long fromAccNo, long toAccNo, BigDecimal amount, String purpose) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account from = arepo.findByAccountNumber(fromAccNo);
        Account to = arepo.findByAccountNumber(toAccNo);

        if (from == null || to == null) {
            throw new IllegalArgumentException("One or both accounts not found");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Update balances
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        arepo.save(from);
        arepo.save(to);

        LocalDateTime now = LocalDateTime.now();

        // Save transaction record for sender and receiver
        saveTransaction(fromAccNo, toAccNo, amount, purpose, "Transfer", from.getBalance(), now);
        saveTransaction(toAccNo, fromAccNo, amount, purpose, "Receive", to.getBalance(), now);

        System.out.println("Transaction completed successfully");
    }

    /**
     * Handles depositing money into an account.
     * Updates balance and logs the deposit transaction.
     */
    @Transactional
    public void deposit(long acno, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account ac = arepo.findByAccountNumber(acno);
        if (ac == null) {
            throw new IllegalArgumentException("Account not found");
        }

        // Update balance using AccountService
        aser.updateBalance(acno, amount, "deposit");

        // Refresh account to get updated balance
        ac = arepo.findByAccountNumber(acno);

        // Save transaction log (other account number = 0)
        saveTransaction(acno, 0L, amount, "Deposit", "Deposit", ac.getBalance(), LocalDateTime.now());
    }

    /**
     * Handles withdrawing money from an account.
     * Updates balance and logs the withdrawal transaction.
     */
    @Transactional
    public void withdraw(long acno, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account ac = arepo.findByAccountNumber(acno);
        if (ac == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (ac.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Update balance using AccountService
        aser.updateBalance(acno, amount, "withdraw");

        // Refresh account to get updated balance
        ac = arepo.findByAccountNumber(acno);

        // Save transaction log (other account number = 0)
        saveTransaction(acno, 0L, amount, "Withdraw", "Withdraw", ac.getBalance(), LocalDateTime.now());
    }

    /**
     * Saves a transaction record to the database.
     */
    private void saveTransaction(long selfAccNo, long otherAccNo, BigDecimal amount, String purpose,
                                 String type, BigDecimal balance, LocalDateTime time) {
        Transaction t = new Transaction();
        t.setSelfAccountNumber(selfAccNo);
        t.setAccountNumber(otherAccNo);
        t.setAmount(amount);
        t.setPurpose(purpose);
        t.setTransactionType(type); // Transfer, Receive, Deposit, Withdraw
        t.setBalance(balance);
        t.setStatus("Completed");
        t.setTime(time);
        trepo.save(t);
    }

    /**
     * Returns all transactions related to the given account.
     */
    public List<Transaction> viewAllTransactions(Account acc) {
        return trepo.findBySelfAccountNumber(acc.getAccountNumber());
    }
}
