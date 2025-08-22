package com.payment.gateway.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.payment.gateway.config.DateTimeSerializer;
import com.payment.gateway.model.Dispute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {
    
    private Long id;
    private String disputeId;
    private String paymentId;
    private String transactionId;
    private String merchantId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private Dispute.DisputeStatus status;
    private Dispute.DisputeReason reason;
    private String description;
    private String evidence;
    private String gatewayResponse;
    private String gatewayDisputeId;
    private LocalDateTime disputeDate;
    private LocalDateTime resolutionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    private boolean success;
}