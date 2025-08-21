package com.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminEvaluationRequest {
    
    private String decision; // APPROVE_MERCHANT, APPROVE_CUSTOMER, PARTIAL_REFUND
    private String evaluation;
    private BigDecimal refundAmount; // Partial refund için
    private String reasoning;
    private String recommendedAction; // REJECT_DISPUTE, APPROVE_DISPUTE, PARTIAL_REFUND
    private String internalNotes;
    private String notes; // Admin notları
}