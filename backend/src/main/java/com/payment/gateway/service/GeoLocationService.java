package com.payment.gateway.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Gerçek GeoIP lokasyon servisi
 * Gerçek banka sistemlerinde kullanılan IP geolocation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Rate limiting için cache kullanıyoruz
    @Cacheable(value = "geoCache", key = "#ipAddress")
    public GeoLocation getLocationSync(String ipAddress) {
        if (isPrivateOrLocalIP(ipAddress)) {
            return createLocalLocation();
        }
        
        try {
            // IP-API.com kullanıyoruz (ücretsiz, 1000 request/month limit)
            // KVKK/GDPR: Exact coordinates (lat/lon) excluded for privacy compliance
            String url = "http://ip-api.com/json/" + ipAddress + "?fields=status,message,country,countryCode,region,regionName,timezone,isp,org,as,query";
            
            log.debug("Fetching geolocation for IP: {}", ipAddress);
            
            IpApiResponse response = restTemplate.getForObject(url, IpApiResponse.class);
            
            if (response != null && "success".equals(response.getStatus())) {
                log.info("GeoLocation found for IP {}: {} - {}", ipAddress, response.getCountry(), response.getRegionName());
                
                return GeoLocation.builder()
                    .ipAddress(ipAddress)
                    .countryCode(response.getCountryCode())
                    .countryName(response.getCountry())
                    .region(response.getRegion())
                    .regionName(response.getRegionName())
                    // KVKK/GDPR: City and exact coordinates removed for privacy
                    .timezone(response.getTimezone())
                    .isp(response.getIsp())
                    .organization(response.getOrg())
                    .autonomousSystem(response.getAs())
                    .riskScore(calculateRiskScore(response))
                    .build();
            } else {
                log.warn("Failed to get geolocation for IP {}: {}", ipAddress, 
                    response != null ? response.getMessage() : "Unknown error");
                return createUnknownLocation(ipAddress);
            }
            
        } catch (Exception e) {
            log.error("Error fetching geolocation for IP {}: {}", ipAddress, e.getMessage());
            return createUnknownLocation(ipAddress);
        }
    }
    
    /**
     * Async geolocation lookup (performance için)
     */
    public CompletableFuture<GeoLocation> getLocationAsync(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> getLocationSync(ipAddress));
    }
    
    /**
     * Private/local IP kontrolü
     */
    private boolean isPrivateOrLocalIP(String ipAddress) {
        if (ipAddress == null) return true;
        
        return ipAddress.equals("127.0.0.1") || 
               ipAddress.equals("0:0:0:0:0:0:0:1") || 
               ipAddress.equals("::1") ||
               ipAddress.startsWith("192.168.") || 
               ipAddress.startsWith("10.") || 
               ipAddress.startsWith("172.16.") || 
               ipAddress.startsWith("172.17.") || 
               ipAddress.startsWith("172.18.") || 
               ipAddress.startsWith("172.19.") || 
               ipAddress.startsWith("172.2") || 
               ipAddress.startsWith("172.30.") || 
               ipAddress.startsWith("172.31.") ||
               ipAddress.startsWith("169.254."); // Link-local
    }
    
    /**
     * Local IP için location oluştur
     */
    private GeoLocation createLocalLocation() {
        return GeoLocation.builder()
            .ipAddress("localhost")
            .countryCode("LOCAL")
            .countryName("Local/Development")
            .regionName("Development Environment")
            .riskScore(0.0) // Local IP'ler risk değil
            .build();
    }
    
    /**
     * Bilinmeyen IP için location oluştur
     */
    private GeoLocation createUnknownLocation(String ipAddress) {
        return GeoLocation.builder()
            .ipAddress(ipAddress)
            .countryCode("UNKNOWN")
            .countryName("Unknown")
            .regionName("Unknown")
            .riskScore(0.5) // Bilinmeyen lokasyonlar orta risk
            .build();
    }
    
    /**
     * GeoLocation verisine göre risk skoru hesaplar
     * Gerçek banka sistemlerinde karmaşık algoritma kullanılır
     */
    private Double calculateRiskScore(IpApiResponse response) {
        double baseRisk = 0.1; // Base risk
        
        // Ülke bazlı risk (örnek)
        String country = response.getCountryCode();
        if (country != null) {
            switch (country.toUpperCase()) {
                case "US", "CA", "GB", "DE", "FR", "NL", "SE", "NO", "DK" -> baseRisk += 0.0; // Düşük risk
                case "TR", "IT", "ES", "PL", "CZ" -> baseRisk += 0.1; // Orta risk
                case "RU", "CN", "IR", "KP" -> baseRisk += 0.4; // Yüksek risk
                default -> baseRisk += 0.2; // Default orta risk
            }
        }
        
        // ISP bazlı risk
        String isp = response.getIsp();
        if (isp != null) {
            if (isp.toLowerCase().contains("hosting") || 
                isp.toLowerCase().contains("vps") || 
                isp.toLowerCase().contains("proxy")) {
                baseRisk += 0.3; // Hosting/VPS/Proxy yüksek risk
            }
        }
        
        // AS (Autonomous System) bazlı kontrol
        String as = response.getAs();
        if (as != null && (as.toLowerCase().contains("tor") || as.toLowerCase().contains("vpn"))) {
            baseRisk += 0.5; // VPN/Tor çok yüksek risk
        }
        
        return Math.min(baseRisk, 1.0); // Max 1.0 risk score
    }
    
    /**
     * IP-API.com response format
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpApiResponse {
        private String status;
        private String message;
        private String country;
        private String countryCode;
        private String region;
        private String regionName;
        // KVKK/GDPR: City and coordinate fields removed for privacy
        private String timezone;
        private String isp;
        private String org;
        private String as;
        private String query;
    }
    
    /**
     * GeoLocation data model - KVKV/GDPR Compliant
     */
    @Data
    @lombok.Builder
    public static class GeoLocation {
        private String ipAddress;
        private String countryCode;
        private String countryName;
        private String region;
        private String regionName;
        // KVKK/GDPR: City, coordinates removed for privacy compliance
        private String timezone;
        private String isp;
        private String organization;
        private String autonomousSystem;
        private Double riskScore; // 0.0 - 1.0 risk score
        
        public String getRiskLevel() {
            if (riskScore == null) return "UNKNOWN";
            if (riskScore < 0.2) return "LOW";
            if (riskScore < 0.5) return "MEDIUM"; 
            if (riskScore < 0.8) return "HIGH";
            return "CRITICAL";
        }
        
        public boolean isHighRisk() {
            return riskScore != null && riskScore >= 0.5;
        }
    }
}