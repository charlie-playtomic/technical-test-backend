package com.playtomic.tests.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    
    private String walletId;
    
    private TransactionType type;
    
    private BigDecimal amount;
    
    private String paymentId; // Reference to Stripe payment ID
    
    private LocalDateTime timestamp;
    
    private TransactionStatus status;
    
    public enum TransactionType {
        CREDIT,
        DEBIT,
        REFUND
    }
    
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
} 