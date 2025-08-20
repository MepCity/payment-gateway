package com.payment.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Request Context bilgilerini yakalayan servis
 * Gerçek banka sistemlerinde kullanılan audit detayları
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestContextService {
    
    private final GeoLocationService geoLocationService;
    
    /**
     * Mevcut HTTP request'ten audit için gerekli bilgileri çıkarır
     */
    public AuditRequestContext extractRequestContext() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return createDefaultContext();
            }
            
            HttpServletRequest request = attributes.getRequest();
            String clientIP = extractClientIpAddress(request);
            
            // Gerçek GeoLocation verisi al
            GeoLocationService.GeoLocation geoData = geoLocationService.getLocationSync(clientIP);
            
            return AuditRequestContext.builder()
                .ipAddress(clientIP)
                .userAgent(request.getHeader("User-Agent"))
                .sessionId(request.getSession(false) != null ? request.getSession().getId() : null)
                .requestMethod(request.getMethod())
                .requestUri(request.getRequestURI())
                .countryCode(geoData.getCountryCode())
                .countryName(geoData.getCountryName())
                .regionName(geoData.getRegionName()) // KVKK/GDPR: Only region, not exact city
                .timezone(geoData.getTimezone())
                .isp(geoData.getIsp())
                .riskScore(geoData.getRiskScore())
                .riskLevel(geoData.getRiskLevel())
                .browserInfo(extractBrowserInfo(request.getHeader("User-Agent")))
                .deviceFingerprint(generateDeviceFingerprint(request))
                .apiKey(maskApiKey(request.getHeader("X-API-Key")))
                .correlationId(request.getHeader("X-Correlation-ID"))
                .requestHeaders(extractImportantHeaders(request))
                .requestSizeBytes(getRequestSize(request))
                .build();
                
        } catch (Exception e) {
            log.warn("Failed to extract request context: {}", e.getMessage());
            return createDefaultContext();
        }
    }
    
    /**
     * Gerçek client IP adresini çıkarır (proxy, load balancer arkasında da çalışır)
     */
    private String extractClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * User-Agent'tan browser bilgilerini çıkarır
     */
    private BrowserInfo extractBrowserInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return new BrowserInfo("Unknown", "Unknown", "Unknown");
        }
        
        String browserName = "Unknown";
        String browserVersion = "Unknown";
        String operatingSystem = "Unknown";
        
        // Browser detection
        if (userAgent.contains("Chrome")) {
            browserName = "Chrome";
            Pattern pattern = Pattern.compile("Chrome/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browserVersion = matcher.group(1);
            }
        } else if (userAgent.contains("Firefox")) {
            browserName = "Firefox";
            Pattern pattern = Pattern.compile("Firefox/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browserVersion = matcher.group(1);
            }
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            browserName = "Safari";
            Pattern pattern = Pattern.compile("Version/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browserVersion = matcher.group(1);
            }
        } else if (userAgent.contains("curl")) {
            browserName = "curl";
            Pattern pattern = Pattern.compile("curl/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                browserVersion = matcher.group(1);
            }
        }
        
        // OS detection
        if (userAgent.contains("Windows")) {
            operatingSystem = "Windows";
        } else if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS")) {
            operatingSystem = "macOS";
        } else if (userAgent.contains("Linux")) {
            operatingSystem = "Linux";
        } else if (userAgent.contains("Android")) {
            operatingSystem = "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            operatingSystem = "iOS";
        }
        
        return new BrowserInfo(browserName, browserVersion, operatingSystem);
    }
    
    /**
     * Device fingerprinting (basit implementasyon)
     */
    private String generateDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprint = new StringBuilder();
        
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");
        String connection = request.getHeader("Connection");
        
        if (userAgent != null) fingerprint.append(userAgent.hashCode());
        if (acceptLanguage != null) fingerprint.append(acceptLanguage.hashCode());
        if (acceptEncoding != null) fingerprint.append(acceptEncoding.hashCode());
        if (connection != null) fingerprint.append(connection.hashCode());
        
        return "FP-" + Math.abs(fingerprint.toString().hashCode());
    }
    
    /**
     * API key'i maskeler (güvenlik için)
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return apiKey;
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * Önemli request header'ları çıkarır
     */
    private String extractImportantHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        
        // Audit için önemli header'lar
        String[] importantHeaders = {
            "Content-Type", "Accept", "Authorization", "X-API-Key", 
            "X-Correlation-ID", "X-Request-ID", "Accept-Language",
            "Cache-Control", "Pragma"
        };
        
        for (String headerName : importantHeaders) {
            String value = request.getHeader(headerName);
            if (value != null) {
                // Authorization header'ı maskele
                if ("Authorization".equals(headerName) && value.length() > 20) {
                    value = value.substring(0, 10) + "****" + value.substring(value.length() - 4);
                }
                headers.put(headerName, value);
            }
        }
        
        return headers.isEmpty() ? null : headers.toString();
    }
    
    /**
     * Request boyutunu hesaplar
     */
    private Long getRequestSize(HttpServletRequest request) {
        try {
            int contentLength = request.getContentLength();
            return contentLength > 0 ? (long) contentLength : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Default context oluşturur (request context olmadığında)
     */
    private AuditRequestContext createDefaultContext() {
        return AuditRequestContext.builder()
            .ipAddress("UNKNOWN")
            .userAgent("SYSTEM")
            .requestMethod("SYSTEM")
            .countryCode("UNKNOWN")
            .browserInfo(new BrowserInfo("System", "N/A", "Server"))
            .build();
    }
    
    /**
     * Browser bilgilerini tutar
     */
    public static class BrowserInfo {
        public final String name;
        public final String version;
        public final String operatingSystem;
        
        public BrowserInfo(String name, String version, String operatingSystem) {
            this.name = name;
            this.version = version;
            this.operatingSystem = operatingSystem;
        }
    }
    
    /**
     * Request context bilgilerini tutar - KVKK/GDPR Compliant
     */
    public static class AuditRequestContext {
        public final String ipAddress;
        public final String userAgent;
        public final String sessionId;
        public final String requestMethod;
        public final String requestUri;
        public final String countryCode;
        public final String countryName;
        public final String regionName; // KVKK/GDPR: Only region, not exact city/coordinates
        public final String timezone;
        public final String isp;
        public final Double riskScore;
        public final String riskLevel;
        public final BrowserInfo browserInfo;
        public final String deviceFingerprint;
        public final String apiKey;
        public final String correlationId;
        public final String requestHeaders;
        public final Long requestSizeBytes;
        
        private AuditRequestContext(Builder builder) {
            this.ipAddress = builder.ipAddress;
            this.userAgent = builder.userAgent;
            this.sessionId = builder.sessionId;
            this.requestMethod = builder.requestMethod;
            this.requestUri = builder.requestUri;
            this.countryCode = builder.countryCode;
            this.countryName = builder.countryName;
            this.regionName = builder.regionName;
            this.timezone = builder.timezone;
            this.isp = builder.isp;
            this.riskScore = builder.riskScore;
            this.riskLevel = builder.riskLevel;
            this.browserInfo = builder.browserInfo;
            this.deviceFingerprint = builder.deviceFingerprint;
            this.apiKey = builder.apiKey;
            this.correlationId = builder.correlationId;
            this.requestHeaders = builder.requestHeaders;
            this.requestSizeBytes = builder.requestSizeBytes;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String ipAddress;
            private String userAgent;
            private String sessionId;
            private String requestMethod;
            private String requestUri;
            private String countryCode;
            private String countryName;
            private String regionName; // KVKK/GDPR: Only region name, not exact location
            private String timezone;
            private String isp;
            private Double riskScore;
            private String riskLevel;
            private BrowserInfo browserInfo;
            private String deviceFingerprint;
            private String apiKey;
            private String correlationId;
            private String requestHeaders;
            private Long requestSizeBytes;
            
            public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
            public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
            public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
            public Builder requestMethod(String requestMethod) { this.requestMethod = requestMethod; return this; }
            public Builder requestUri(String requestUri) { this.requestUri = requestUri; return this; }
            public Builder countryCode(String countryCode) { this.countryCode = countryCode; return this; }
            public Builder countryName(String countryName) { this.countryName = countryName; return this; }
            public Builder regionName(String regionName) { this.regionName = regionName; return this; }
            public Builder timezone(String timezone) { this.timezone = timezone; return this; }
            public Builder isp(String isp) { this.isp = isp; return this; }
            public Builder riskScore(Double riskScore) { this.riskScore = riskScore; return this; }
            public Builder riskLevel(String riskLevel) { this.riskLevel = riskLevel; return this; }
            public Builder browserInfo(BrowserInfo browserInfo) { this.browserInfo = browserInfo; return this; }
            public Builder deviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; return this; }
            public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
            public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
            public Builder requestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; return this; }
            public Builder requestSizeBytes(Long requestSizeBytes) { this.requestSizeBytes = requestSizeBytes; return this; }
            
            public AuditRequestContext build() {
                return new AuditRequestContext(this);
            }
        }
    }
}