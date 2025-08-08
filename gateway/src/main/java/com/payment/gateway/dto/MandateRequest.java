package com.payment.gateway.dto;

import com.payment.gateway.model.Mandate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MandateRequest {
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Amount cannot exceed 999999.99")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;
    
    @NotNull(message = "Mandate type is required")
    private Mandate.MandateType type;
    
    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^[0-9]{8,10}$", message = "Bank account number must be 8-10 digits")
    private String bankAccountNumber;
    
    @NotBlank(message = "Bank sort code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Bank sort code must be 6 digits")
    private String bankSortCode;
    
    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    private String accountHolderName;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @NotNull(message = "Frequency is required")
    @Min(value = 1, message = "Frequency must be at least 1 day")
    @Max(value = 365, message = "Frequency cannot exceed 365 days")
    private Integer frequency;
    
    @NotNull(message = "Max payments is required")
    @Min(value = -1, message = "Max payments must be -1 (unlimited) or positive")
    private Integer maxPayments;
}
