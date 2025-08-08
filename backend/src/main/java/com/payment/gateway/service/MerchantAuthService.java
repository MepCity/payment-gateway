package com.payment.gateway.service;

import com.payment.gateway.model.Merchant;
import com.payment.gateway.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantAuthService {
    
    private final MerchantRepository merchantRepository;
    
    /**
     * API key ile merchant doğrulama
     */
    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("🚫 Boş API key ile istek geldi");
            return false;
        }
        
        Optional<Merchant> merchant = merchantRepository.findByApiKey(apiKey);
        
        if (merchant.isEmpty()) {
            log.warn("🚫 Geçersiz API key: {}", apiKey);
            return false;
        }
        
        if (merchant.get().getStatus() != Merchant.MerchantStatus.ACTIVE) {
            log.warn("🚫 Pasif merchant'tan istek: {} - Status: {}", 
                merchant.get().getMerchantId(), merchant.get().getStatus());
            return false;
        }
        
        log.info("✅ Geçerli API key - Merchant: {}", merchant.get().getMerchantId());
        return true;
    }
    
    /**
     * API key'den merchant bilgisi al
     */
    public Optional<Merchant> getMerchantByApiKey(String apiKey) {
        return merchantRepository.findByApiKey(apiKey);
    }
    
    /**
     * Merchant ID ile API key eşleşmesi kontrol et
     */
    public boolean validateMerchantAccess(String apiKey, String merchantId) {
        Optional<Merchant> merchant = getMerchantByApiKey(apiKey);
        
        if (merchant.isEmpty()) {
            log.warn("🚫 API key bulunamadı: {}", apiKey);
            return false;
        }
        
        if (!merchant.get().getMerchantId().equals(merchantId)) {
            log.warn("🚫 Merchant ID uyumsuzluğu - API Key: {} için {} kullanılamaz", 
                apiKey, merchantId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Yeni API key oluştur
     */
    public String generateApiKey(String merchantId) {
        // Production'da güvenli rastgele string üretilmeli
        return "pk_" + merchantId.toLowerCase() + "_" + System.currentTimeMillis();
    }
}
