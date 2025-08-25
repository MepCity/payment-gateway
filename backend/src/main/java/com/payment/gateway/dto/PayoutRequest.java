package com.payment.gateway.dto;

import com.payment.gateway.model.Payout;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequest {
    
    @NotBlank(message = "Merchant ID is required")
    @Size(min = 3, max = 50, message = "Merchant ID must be between 3 and 50 characters")
    private String merchantId;
    
    @NotBlank(message = "Customer ID is required")
    @Size(min = 3, max = 50, message = "Customer ID must be between 3 and 50 characters")
    private String customerId;
    
    @NotBlank(message = "Payment ID is required")
    @Size(min = 3, max = 50, message = "Payment ID must be between 3 and 50 characters")
    private String paymentId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Amount cannot exceed 999,999.99")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be in ISO 4217 format (e.g., USD, EUR)")
    private String currency;
    
    @NotNull(message = "Payout type is required")
    private Payout.PayoutType type;
    
    @NotBlank(message = "Bank account number is required")
    @Size(min = 8, max = 34, message = "Bank account number must be between 8 and 34 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Bank account number must contain only digits")
    private String bankAccountNumber;
    
    @NotBlank(message = "Bank routing number is required")
    @Size(min = 8, max = 12, message = "Bank routing number must be between 8 and 12 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Bank routing number must contain only digits")
    private String bankRoutingNumber;
    
    @NotBlank(message = "Bank name is required")
    @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
    private String bankName;
    
    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Account holder name must contain only letters and spaces")
    private String accountHolderName;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}