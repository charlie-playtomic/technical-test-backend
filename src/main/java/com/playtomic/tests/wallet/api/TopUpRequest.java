package com.playtomic.tests.wallet.api;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest {
    private String creditCardNumber;
    private BigDecimal amount;
} 