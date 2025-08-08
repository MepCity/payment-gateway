package com.payment.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

/**
 * Türk bankalarının test ortamlarını simüle eden servis
 * Gerçek entegrasyon için her bankanın API'si ayrı ayrı implement edilmeli
 */
@Service
@Slf4j
public class BankTestService {
    
    // Türk bankalarının test kartları
    private static final Map<String, BankTestResult> TEST_CARDS = new HashMap<>();
    
    static {
        // Garanti BBVA Test Kartları
        TEST_CARDS.put("4824940000000014", new BankTestResult("SUCCESS", "00", "Başarılı işlem"));
        TEST_CARDS.put("4824940000000022", new BankTestResult("FAILED", "51", "Yetersiz bakiye"));
        TEST_CARDS.put("4824940000000030", new BankTestResult("3D_SECURE", "3D", "3D Secure doğrulama gerekli"));
        TEST_CARDS.put("4824940000000048", new BankTestResult("FAILED", "05", "İşlem reddedildi"));
        
        // İş Bankası Test Kartları
        TEST_CARDS.put("4508034508034509", new BankTestResult("SUCCESS", "00", "Başarılı işlem"));
        TEST_CARDS.put("4508034508034517", new BankTestResult("FAILED", "54", "Vadesi geçmiş kart"));
        TEST_CARDS.put("4508034508034525", new BankTestResult("3D_SECURE", "3D", "3D Secure doğrulama gerekli"));
        
        // Yapı Kredi Test Kartları
        TEST_CARDS.put("4090650000000014", new BankTestResult("SUCCESS", "00", "Başarılı işlem"));
        TEST_CARDS.put("4090650000000022", new BankTestResult("FAILED", "05", "İşlem reddedildi"));
        
        // Akbank Test Kartları
        TEST_CARDS.put("4355080000000006", new BankTestResult("SUCCESS", "00", "Başarılı işlem"));
        TEST_CARDS.put("4355080000000014", new BankTestResult("FAILED", "57", "İşlem yapılmasına izin verilmez"));
        
        // Ziraat Bankası Test Kartları
        TEST_CARDS.put("4546771111111118", new BankTestResult("SUCCESS", "00", "Başarılı işlem"));
        TEST_CARDS.put("4546771111111126", new BankTestResult("FAILED", "61", "Para çekme limiti aşıldı"));
        
        // Genel test senaryoları
        TEST_CARDS.put("4111111111111111", new BankTestResult("SUCCESS", "00", "Visa test kartı"));
        TEST_CARDS.put("5555555555554444", new BankTestResult("SUCCESS", "00", "Mastercard test kartı"));
        TEST_CARDS.put("4000000000000002", new BankTestResult("FAILED", "05", "İşlem reddedildi"));
        TEST_CARDS.put("4000000000000069", new BankTestResult("FAILED", "54", "Vadesi geçmiş kart"));
        TEST_CARDS.put("4000000000000119", new BankTestResult("FAILED", "14", "Geçersiz kart numarası"));
    }
    
    /**
     * Kart numarasına göre test sonucu döner
     * Gerçek implementasyonda her bankanın API'sine istek atılır
     */
    public BankTestResult processTestPayment(String cardNumber, String amount, String currency) {
        log.info("Test ödeme işlemi: Kart={}, Tutar={} {}", maskCardNumber(cardNumber), amount, currency);
        
        // Kart numarasından boşlukları temizle
        String cleanCardNumber = cardNumber.replaceAll("\\s+", "");
        
        // Test kartı kontrolü
        BankTestResult result = TEST_CARDS.get(cleanCardNumber);
        if (result != null) {
            log.info("Test kartı bulundu: {} - {}", result.getStatus(), result.getMessage());
            return result;
        }
        
        // Test kartı değilse, son hanelere göre simülasyon
        if (cleanCardNumber.endsWith("0000")) {
            return new BankTestResult("FAILED", "51", "Yetersiz bakiye");
        } else if (cleanCardNumber.endsWith("1111")) {
            return new BankTestResult("FAILED", "05", "İşlem reddedildi");
        } else if (cleanCardNumber.endsWith("2222")) {
            return new BankTestResult("3D_SECURE", "3D", "3D Secure doğrulama gerekli");
        } else {
            return new BankTestResult("SUCCESS", "00", "Başarılı işlem");
        }
    }
    
    /**
     * 3D Secure doğrulama simülasyonu
     */
    public BankTestResult process3DSecure(String cardNumber, String password) {
        log.info("3D Secure doğrulama: Kart={}", maskCardNumber(cardNumber));
        
        // Test şifreleri
        if ("123456".equals(password)) {
            return new BankTestResult("SUCCESS", "00", "3D Secure doğrulama başarılı");
        } else {
            return new BankTestResult("FAILED", "3D", "3D Secure doğrulama başarısız");
        }
    }
    
    /**
     * Banka hata kodlarını açıklama
     */
    public String getErrorDescription(String errorCode) {
        Map<String, String> errorCodes = Map.of(
            "00", "Başarılı işlem",
            "05", "İşlem reddedildi",
            "51", "Yetersiz bakiye",
            "54", "Vadesi geçmiş kart",
            "57", "İşlem yapılmasına izin verilmez",
            "61", "Para çekme limiti aşıldı",
            "65", "Günlük işlem sayısı aşıldı",
            "14", "Geçersiz kart numarası",
            "3D", "3D Secure doğrulama gerekli"
        );
        
        return errorCodes.getOrDefault(errorCode, "Bilinmeyen hata");
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return "****";
        }
        String clean = cardNumber.replaceAll("\\s+", "");
        return clean.substring(0, 4) + "****" + clean.substring(clean.length() - 4);
    }
    
    // Test sonucu modeli
    public static class BankTestResult {
        private final String status;
        private final String errorCode;
        private final String message;
        
        public BankTestResult(String status, String errorCode, String message) {
            this.status = status;
            this.errorCode = errorCode;
            this.message = message;
        }
        
        public String getStatus() { return status; }
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
        
        public boolean is3DSecureRequired() {
            return "3D_SECURE".equals(status);
        }
    }
}
