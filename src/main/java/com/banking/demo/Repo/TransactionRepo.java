package com.banking.demo.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.banking.demo.Models.Transaction;
import java.util.List;


@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {
   
   List<Transaction> findBySelfAccountNumber(long selfAccountNumber);
}
