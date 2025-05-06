package com.playtomic.tests.wallet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = Wallet.builder()
                .id("test-wallet")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void credit_ShouldIncreaseBalance() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        LocalDateTime beforeUpdate = wallet.getUpdatedAt();

        // When
        wallet.credit(amount);

        // Then
        assertEquals(amount, wallet.getBalance());
        assertTrue(wallet.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void credit_ShouldThrowException_WhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.credit(new BigDecimal("-10.00"));
        });
    }

    @Test
    void credit_ShouldThrowException_WhenAmountIsZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.credit(BigDecimal.ZERO);
        });
    }

    @Test
    void debit_ShouldDecreaseBalance() {
        // Given
        wallet.credit(new BigDecimal("100.00"));
        BigDecimal debitAmount = new BigDecimal("50.00");
        LocalDateTime beforeUpdate = wallet.getUpdatedAt();

        // When
        wallet.debit(debitAmount);

        // Then
        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
        assertTrue(wallet.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void debit_ShouldThrowException_WhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.debit(new BigDecimal("-10.00"));
        });
    }

    @Test
    void debit_ShouldThrowException_WhenAmountIsZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.debit(BigDecimal.ZERO);
        });
    }

    @Test
    void debit_ShouldThrowException_WhenInsufficientFunds() {
        // Given
        wallet.credit(new BigDecimal("50.00"));

        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            wallet.debit(new BigDecimal("100.00"));
        });
    }
} 