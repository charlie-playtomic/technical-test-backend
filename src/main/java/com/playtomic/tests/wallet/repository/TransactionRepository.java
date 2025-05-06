package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByWalletIdOrderByTimestampDesc(String walletId);
} 