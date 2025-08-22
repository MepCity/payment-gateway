package com.payment.gateway.dto;

import com.payment.gateway.model.Mandate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MandateResponse {
    
    private Long id;
    private String mandateId;
    private String customerId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private Mandate.MandateStatus status;
    private Mandate.MandateType type;
    private String bankAccountNumber;
    private String bankSortCode;
    private String accountHolderName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer frequency;
    private Integer maxPayments;
    private String gatewayResponse;
    private String gatewayMandateId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    private boolean success;
}