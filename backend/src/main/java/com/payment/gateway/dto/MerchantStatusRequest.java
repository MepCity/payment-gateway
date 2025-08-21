package com.payment.gateway.dto;

import com.payment.gateway.model.Merchant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStatusRequest {
    
    @NotNull(message = "Status zorunludur")
    private Merchant.MerchantStatus status;
    
    private String reason;
}