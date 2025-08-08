package com.payment.gateway.dto;

import com.payment.gateway.model.Payout;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {
    
    private boolean success;
    private String message;
    private String payoutId;
    private String merchantId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private Payout.PayoutStatus status;
    private Payout.PayoutType type;
    private String maskedBankAccountNumber;
    private String bankName;
    private String accountHolderName;
    private String description;
    private String gatewayPayoutId;
    private LocalDateTime processedAt;
    private LocalDateTime settledAt;
    private String failureReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
