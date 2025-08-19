package com.payment.gateway.dto;

import com.payment.gateway.model.Merchant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantRequest {
    
    @NotBlank(message = "Merchant ID zorunludur")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Merchant ID sadece harf, rakam, tire ve alt çizgi içerebilir")
    private String merchantId;
    
    @NotBlank(message = "Merchant adı zorunludur")
    private String name;
    
    @NotBlank(message = "Email zorunludur")
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
    
    public Merchant toEntity() {
        Merchant merchant = new Merchant();
        merchant.setMerchantId(this.merchantId);
        merchant.setName(this.name);
        merchant.setEmail(this.email);
        merchant.setPhone(this.phone);
        merchant.setAddress(this.address);
        merchant.setWebsite(this.website);
        merchant.setWebhookUrl(this.webhookUrl);
        merchant.setWebhookEvents(this.webhookEvents);
        return merchant;
    }
}
