package com.payment.gateway.service;

import com.payment.gateway.adapter.BankAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Bank Adapter Registry
 * Tüm bank adapter'larını yönetir ve kart numarasına göre uygun adapter'ı bulur
 */
@Service
@Slf4j
public class BankAdapterRegistry {
    
    private final Map<String, BankAdapter> adaptersByBankName = new HashMap<>();
    private final List<BankAdapter> allAdapters;
    
    @Autowired
    public BankAdapterRegistry(List<BankAdapter> bankAdapters) {
        this.allAdapters = bankAdapters;
        
        // Adapter'ları banka adına göre map'le
        for (BankAdapter adapter : bankAdapters) {
            adaptersByBankName.put(adapter.getBankName(), adapter);
            log.info("Registered bank adapter: {} - Format: {} -> {}", 
                    adapter.getBankName(), 
                    adapter.getRequestFormat(), 
                    adapter.getResponseFormat());
        }
        
        log.info("Total {} bank adapters registered", bankAdapters.size());
    }
    
    /**
     * Kart numarasına göre uygun bank adapter'ı bul
     */
    public Optional<BankAdapter> findAdapterByCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            log.warn("Invalid card number for adapter lookup: {}", cardNumber);
            return Optional.empty();
        }
        
        log.debug("Looking for adapter for card: {}", maskCardNumber(cardNumber));
        
        for (BankAdapter adapter : allAdapters) {
            if (adapter.isConfigured() && adapter.supportsBin(cardNumber)) {
                log.info("Found adapter for card {}: {} ({})", 
                        maskCardNumber(cardNumber), 
                        adapter.getBankName(),
                        adapter.getRequestFormat());
                return Optional.of(adapter);
            }
        }
        
        log.warn("No adapter found for card: {}", maskCardNumber(cardNumber));
        return Optional.empty();
    }
    
    /**
     * Banka adına göre adapter bul
     */
    public Optional<BankAdapter> findAdapterByBankName(String bankName) {
        BankAdapter adapter = adaptersByBankName.get(bankName);
        if (adapter != null && adapter.isConfigured()) {
            return Optional.of(adapter);
        }
        return Optional.empty();
    }
    
    /**
     * Tüm aktif adapter'ları getir
     */
    public List<BankAdapter> getAllActiveAdapters() {
        return allAdapters.stream()
                .filter(BankAdapter::isConfigured)
                .toList();
    }
    
    /**
     * Adapter istatistikleri
     */
    public Map<String, Object> getAdapterStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalAdapters = allAdapters.size();
        long activeAdapters = allAdapters.stream()
                .mapToLong(adapter -> adapter.isConfigured() ? 1 : 0)
                .sum();
        
        Map<String, Long> formatStats = new HashMap<>();
        allAdapters.stream()
                .filter(BankAdapter::isConfigured)
                .forEach(adapter -> {
                    String format = adapter.getRequestFormat().name();
                    formatStats.put(format, formatStats.getOrDefault(format, 0L) + 1);
                });
        
        stats.put("totalAdapters", totalAdapters);
        stats.put("activeAdapters", activeAdapters);
        stats.put("formatDistribution", formatStats);
        
        Map<String, Object> adapterDetails = new HashMap<>();
        allAdapters.forEach(adapter -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("configured", adapter.isConfigured());
            detail.put("testMode", adapter.isTestMode());
            detail.put("requestFormat", adapter.getRequestFormat());
            detail.put("responseFormat", adapter.getResponseFormat());
            adapterDetails.put(adapter.getBankName(), detail);
        });
        stats.put("adapters", adapterDetails);
        
        return stats;
    }
    
    /**
     * Belirli bir format kullanan adapter'ları getir
     */
    public List<BankAdapter> getAdaptersByFormat(BankAdapter.RequestFormat format) {
        return allAdapters.stream()
                .filter(adapter -> adapter.isConfigured())
                .filter(adapter -> adapter.getRequestFormat() == format)
                .toList();
    }
    
    /**
     * Test modu adapter'larını getir
     */
    public List<BankAdapter> getTestModeAdapters() {
        return allAdapters.stream()
                .filter(adapter -> adapter.isConfigured())
                .filter(BankAdapter::isTestMode)
                .toList();
    }
    
    /**
     * Kart numarasını maskele
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * Adapter sağlık kontrolü
     */
    public Map<String, Boolean> performHealthCheck() {
        Map<String, Boolean> healthStatus = new HashMap<>();
        
        for (BankAdapter adapter : allAdapters) {
            try {
                boolean healthy = adapter.isConfigured();
                healthStatus.put(adapter.getBankName(), healthy);
                
                if (!healthy) {
                    log.warn("Adapter {} is not configured properly", adapter.getBankName());
                }
            } catch (Exception e) {
                log.error("Health check failed for adapter {}: {}", adapter.getBankName(), e.getMessage());
                healthStatus.put(adapter.getBankName(), false);
            }
        }
        
        return healthStatus;
    }
}
