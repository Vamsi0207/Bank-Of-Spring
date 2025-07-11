package com.banking.demo.Repo;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banking.demo.Models.Account;

@Repository
public interface AccountRepo extends JpaRepository<Account, Long> {
    Account findByAccountNumber(long accno);

    Account findByEmail(String email);
}
