package com.payment.gateway.dto;

import com.payment.gateway.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private String paymentId;
    private String transactionId;
    private String merchantId;
    private String customerId;
    private String amount;
    private String currency;
    private Payment.PaymentStatus status;
    private Payment.PaymentMethod paymentMethod;
    private String cardNumber;
    private String cardHolderName;
    private String cardBrand;
    private String cardBin;
    private String cardLastFour;
    private String description;
    private String gatewayResponse;
    private String gatewayTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String message;
    private boolean success;
}
