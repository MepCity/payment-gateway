package com.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDisputeNotification {
    
    private String bankDisputeId;
    private String paymentId;
    private String transactionId;
    private String merchantId;
    private BigDecimal disputeAmount;
    private String currency;
    private String disputeReason;
    private LocalDateTime disputeDate;
    private LocalDateTime responseDeadline;
    private String bankName;
    private String customerInfo;
    private LocalDateTime originalTransactionDate;
    private String additionalInfo;
}