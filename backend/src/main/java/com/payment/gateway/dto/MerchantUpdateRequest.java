package com.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantUpdateRequest {
    
    private String name;
    
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Geçerli bir telefon numarası giriniz")
    private String phone;
    
    private String address;
    
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", 
             message = "Geçerli bir website URL'i giriniz")
    private String website;
    
    private String webhookUrl;
    
    private String webhookEvents;
}
