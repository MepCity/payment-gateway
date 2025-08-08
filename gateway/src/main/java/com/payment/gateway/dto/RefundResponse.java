package com.payment.gateway.dto;

import com.payment.gateway.model.Refund;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    
    private Long id;
    private String refundId;
    private String paymentId;
    private String transactionId;
    private String merchantId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private Refund.RefundStatus status;
    private Refund.RefundReason reason;
    private String description;
    private String gatewayResponse;
    private String gatewayRefundId;
    private LocalDateTime refundDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    private boolean success;
}
