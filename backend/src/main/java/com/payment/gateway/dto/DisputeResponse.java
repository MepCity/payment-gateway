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
    
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime disputeDate;
    
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime resolutionDate;
    
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime createdAt;
    
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime updatedAt;
    private String message;
    private boolean success;
    
    // Bank dispute için ek alanlar
    private String bankDisputeId;
    
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime merchantResponseDeadline;
    
    @JsonSerialize(using = DateTimeSerializer.class)
    private LocalDateTime adminEvaluationDeadline;
    private String merchantResponse;
    private String adminNotes;
}