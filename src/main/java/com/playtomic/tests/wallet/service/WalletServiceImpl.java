package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final StripeService stripeService;
    
    private static final int MAX_RETRIES = 3;

    @Override
    @Transactional(readOnly = true)
    public Optional<Wallet> getWallet(String id) {
        return walletRepository.findById(id);
    }

    @Override
    @Transactional
    public Wallet topUp(String walletId, String creditCardNumber, BigDecimal amount) throws StripeServiceException {
        // Create a pending transaction
        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .type(Transaction.TransactionType.CREDIT)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        transactionRepository.save(transaction);
        
        try {
            // Process payment through Stripe
            Payment payment = stripeService.charge(creditCardNumber, amount);
            transaction.setPaymentId(payment.getId());
            
            // Update wallet with retry mechanism for concurrent modifications
            Wallet updatedWallet = updateWalletWithRetry(walletId, amount);
            
            // Update transaction status
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            
            return updatedWallet;
            
        } catch (Exception e) {
            // Mark transaction as failed
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }

    private Wallet updateWalletWithRetry(String walletId, BigDecimal amount) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                Wallet wallet = walletRepository.findById(walletId)
                        .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
                wallet.credit(amount);
                return walletRepository.save(wallet);
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts == MAX_RETRIES) {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Failed to update wallet after " + MAX_RETRIES + " attempts");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(String walletId) {
        return transactionRepository.findByWalletIdOrderByTimestampDesc(walletId);
    }
} 