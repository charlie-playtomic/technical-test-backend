package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.model.Transaction;
import com.playtomic.tests.wallet.model.Wallet;
import com.playtomic.tests.wallet.service.StripeServiceException;
import com.playtomic.tests.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWallet(@PathVariable String id) {
        return walletService.getWallet(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/top-up")
    public ResponseEntity<?> topUp(
            @PathVariable String id,
            @RequestBody TopUpRequest request) {
        try {
            Wallet wallet = walletService.topUp(id, request.getCreditCardNumber(), request.getAmount());
            return ResponseEntity.ok(wallet);
        } catch (StripeServiceException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String id) {
        return ResponseEntity.ok(walletService.getTransactionHistory(id));
    }
}
