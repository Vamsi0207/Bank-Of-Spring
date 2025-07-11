package com.banking.demo.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.banking.demo.Models.Account;
import com.banking.demo.Repo.AccountRepo;

@Service
public class AccountService {

    @Autowired
    private AccountRepo arepo;

    // Used to hash user passwords securely
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Generates a random 8-digit account number
    private long accountNumber() {
        Random random = new Random();
        return 10000000L + random.nextInt(90000000);
    }

    /**
     * Adds a new account to the system.
     * Performs duplicate account number and email checks before saving.
     * Returns the generated account number.
     */
    public long addAccount(String userName, String govtId, BigDecimal balance, String role,
                           String mobileNumber, String email, String gender, String password) {
        long accno = accountNumber();
        int attempts = 0;

        // Retry if generated account number already exists (up to 10 times)
        while (arepo.findByAccountNumber(accno) != null && attempts < 10) {
            accno = accountNumber();
            attempts++;
        }

        if (attempts == 10) {
            throw new RuntimeException("Unable to generate unique account number. Please try again.");
        }

        // Prevent duplicate email registration
        if (arepo.findByEmail(email) != null) {
            throw new RuntimeException("User with this email exists already");
        }

        // Create and populate account object
        Account a = new Account();
        a.setAccountNumber(accno);
        a.setUserName(userName);
        a.setBalance(balance);
        a.setGovtIdNumber(govtId);
        a.setOpenedOn(LocalDate.now());
        a.setRole(role);
        a.setEmail(email);
        a.setMobileNumber(mobileNumber);
        a.setGender(gender);
        a.setPassword(passwordEncoder.encode(password)); // Store hashed password

        // Save to database
        arepo.save(a);
        return accno;
    }

    /**
     * Fetches account details by account number.
     */
    public Account getAccountDetails(long accountNumber) {
        return arepo.findByAccountNumber(accountNumber);
    }

    /**
     * Deletes an account if it exists.
     * Returns true if deleted, false if not found.
     */
    public boolean deleteAccount(long accountNumber) {
        Account account = arepo.findByAccountNumber(accountNumber);
        if (account != null) {
            arepo.delete(account);
            System.out.println("Account Deleted");
            return true;
        } else {
            System.out.println("Account not found");
            return false;
        }
    }

    /**
     * Updates balance for a deposit or withdrawal.
     * Returns true if operation successful, false otherwise.
     */
    public boolean updateBalance(long acno, BigDecimal transferAmount, String transactionType) {
        // Prevent zero or negative transfers
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be positive");
            return false;
        }

        Account a = arepo.findByAccountNumber(acno);
        if (a == null) {
            System.out.println("Account not found");
            return false;
        }

        // Handle deposit
        if ("deposit".equalsIgnoreCase(transactionType)) {
            a.setBalance(a.getBalance().add(transferAmount));
            arepo.save(a);
            System.out.println("Balance updated (deposit/receive)");
            return true;

        // Handle withdrawal
        } else if ("withdraw".equalsIgnoreCase(transactionType)) {
            if (a.getBalance().compareTo(transferAmount) >= 0) {
                a.setBalance(a.getBalance().subtract(transferAmount));
                arepo.save(a);
                System.out.println("Balance updated (withdraw/transfer)");
                return true;
            } else {
                System.out.println("Insufficient balance");
                return false;
            }

        // Invalid transaction type
        } else {
            System.out.println("Invalid transaction type");
            return false;
        }
    }
}
