package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.Payment;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class WalletServiceIT {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private StripeService stripeService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        // Create test wallet
        testWallet = Wallet.builder()
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testWallet = walletRepository.save(testWallet);
    }

    @Test
    void getWallet_ShouldReturnWallet() {
        assertTrue(walletService.getWallet(testWallet.getId()).isPresent());
    }

    @Test
    void getWallet_ShouldReturnEmpty_WhenWalletNotFound() {
        assertTrue(walletService.getWallet("non-existent-id").isEmpty());
    }

    @Test
    void topUp_ShouldUpdateBalanceAndCreateTransaction() throws Exception {
        // Given
        BigDecimal amount = new BigDecimal("50.00");
        String creditCardNumber = "4242 4242 4242 4242";
        when(stripeService.charge(any(), any())).thenReturn(new Payment("payment_id_123"));

        // When
        Wallet updatedWallet = walletService.topUp(testWallet.getId(), creditCardNumber, amount);

        // Then
        assertEquals(amount, updatedWallet.getBalance());
        
        List<Transaction> transactions = walletService.getTransactionHistory(testWallet.getId());
        assertEquals(1, transactions.size());
        
        Transaction transaction = transactions.get(0);
        assertEquals(Transaction.TransactionType.CREDIT, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals("payment_id_123", transaction.getPaymentId());
    }

    @Test
    void getTransactionHistory_ShouldReturnTransactionsInOrder() throws Exception {
        // Given
        when(stripeService.charge(any(), any())).thenReturn(new Payment("payment_id_1"));
        walletService.topUp(testWallet.getId(), "4242 4242 4242 4242", new BigDecimal("10.00"));
        
        when(stripeService.charge(any(), any())).thenReturn(new Payment("payment_id_2"));
        walletService.topUp(testWallet.getId(), "4242 4242 4242 4242", new BigDecimal("20.00"));

        // When
        List<Transaction> transactions = walletService.getTransactionHistory(testWallet.getId());

        // Then
        assertEquals(2, transactions.size());
        assertEquals(new BigDecimal("20.00"), transactions.get(0).getAmount());
        assertEquals(new BigDecimal("10.00"), transactions.get(1).getAmount());
    }
} 