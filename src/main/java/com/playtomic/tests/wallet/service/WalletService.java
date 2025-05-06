package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletService {
    Optional<Wallet> getWallet(String id);
    
    Wallet topUp(String walletId, String creditCardNumber, BigDecimal amount) throws StripeServiceException;
    
    List<Transaction> getTransactionHistory(String walletId);
} 