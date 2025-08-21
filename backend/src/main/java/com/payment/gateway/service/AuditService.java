package com.payment.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.model.AuditLog;
import com.payment.gateway.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final RequestContextService requestContextService;
    
    /**
     * Asynchronously log an audit event
     */
    @Async
    @Transactional
    public void logEvent(AuditEventBuilder builder) {
        try {
            AuditLog auditLog = builder.build();
            auditLogRepository.save(auditLog);
            
            // Also log to application log for immediate visibility
            log.info("AUDIT: {} - {} - {} - {} - {}", 
                auditLog.getEventType(),
                auditLog.getActor(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId());
                
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
            // Fallback to application log
            log.warn("AUDIT_FALLBACK: {} - {} - {}", 
                builder.eventType, builder.actor, builder.action);
        }
    }
    
    /**
     * Create audit event builder with automatic request context injection
     */
    public AuditEventBuilder createEvent() {
        AuditEventBuilder builder = new AuditEventBuilder(this);
        
        // Otomatik olarak request context bilgilerini ekle
        try {
            RequestContextService.AuditRequestContext context = requestContextService.extractRequestContext();
            
            builder.ipAddress(context.ipAddress)
                   .userAgent(context.userAgent)
                   .sessionId(context.sessionId)
                   .requestMethod(context.requestMethod)
                   .requestUri(context.requestUri)
                   .countryCode(context.countryCode)
                   .regionName(context.regionName) // KVKK/GDPR: Only region, not exact city
                   .deviceFingerprint(context.deviceFingerprint)
                   .apiKey(context.apiKey)
                   .correlationId(context.correlationId)
                   .requestHeaders(context.requestHeaders)
                   .requestSizeBytes(context.requestSizeBytes);
                   
            if (context.browserInfo != null) {
                builder.browserName(context.browserInfo.name)
                       .browserVersion(context.browserInfo.version)
                       .operatingSystem(context.browserInfo.operatingSystem);
            }
            
        } catch (Exception e) {
            log.warn("Failed to extract request context for audit: {}", e.getMessage());
        }
        
        return builder;
    }
    
    /**
     * Log authentication events
     */
    public void logAuthentication(String actor, boolean success, String ipAddress, String userAgent) {
        createEvent()
            .eventType(success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE")
            .severity(success ? AuditLog.Severity.LOW : AuditLog.Severity.MEDIUM)
            .actor(actor)
            .action("AUTHENTICATE")
            .resourceType("USER")
            .resourceId(actor)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .complianceTag("PCI_DSS")
            .log();
    }
    
    /**
     * Log payment events
     */
    public void logPayment(String eventType, String paymentId, String merchantId, 
                          String customerId, Object paymentData, String ipAddress) {
        createEvent()
            .eventType(eventType)
            .severity(AuditLog.Severity.MEDIUM)
            .actor("system")
            .action("PROCESS")
            .resourceType("PAYMENT")
            .resourceId(paymentId)
            .additionalData("merchantId", merchantId)
            .additionalData("customerId", customerId)
            .newValues(paymentData)
            .ipAddress(ipAddress)
            .complianceTag("PCI_DSS")
            .complianceTag("GDPR")
            .log();
    }
    
    /**
     * Log blacklist events
     */
    public void logBlacklist(String action, String type, String maskedValue, 
                           String reason, String actor, Object oldData, Object newData) {
        createEvent()
            .eventType("BLACKLIST_ENTRY_" + action.toUpperCase())
            .severity(AuditLog.Severity.HIGH)
            .actor(actor)
            .action(action)
            .resourceType("BLACKLIST")
            .resourceId(type + ":" + maskedValue)
            .oldValues(oldData)
            .newValues(newData)
            .additionalData("reason", reason)
            .complianceTag("PCI_DSS")
            .log();
    }
    
    /**
     * Log fraud detection events
     */
    public void logFraudDetection(String paymentId, String riskLevel, 
                                 double riskScore, List<String> riskFactors, String decision) {
        createEvent()
            .eventType("FRAUD_DETECTION")
            .severity(mapRiskLevelToSeverity(riskLevel))
            .actor("fraud-engine")
            .action("ASSESS")
            .resourceType("PAYMENT")
            .resourceId(paymentId)
            .additionalData("riskLevel", riskLevel)
            .additionalData("riskScore", riskScore)
            .additionalData("riskFactors", riskFactors)
            .additionalData("decision", decision)
            .complianceTag("PCI_DSS")
            .log();
    }
    
    /**
     * Log security events
     */
    public void logSecurityEvent(String eventType, String actor, String details, 
                               String ipAddress, String userAgent) {
        createEvent()
            .eventType(eventType)
            .severity(AuditLog.Severity.HIGH)
            .actor(actor)
            .action("SECURITY_VIOLATION")
            .resourceType("SYSTEM")
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .additionalData("details", details)
            .complianceTag("PCI_DSS")
            .complianceTag("GDPR")
            .log();
    }
    
    private AuditLog.Severity mapRiskLevelToSeverity(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> AuditLog.Severity.CRITICAL;
            case "HIGH" -> AuditLog.Severity.HIGH;
            case "MEDIUM" -> AuditLog.Severity.MEDIUM;
            default -> AuditLog.Severity.LOW;
        };
    }
    
    private String toJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return object.toString();
        }
    }
    
    /**
     * Builder pattern for audit events
     */
    public static class AuditEventBuilder {
        private final AuditService auditService;
        private String eventType;
        private AuditLog.Severity severity = AuditLog.Severity.LOW;
        private String actor;
        private String resourceType;
        private String resourceId;
        private String action;
        private Object oldValues;
        private Object newValues;
        private String ipAddress;
        private String userAgent;
        private String sessionId;
        
        // Yeni gelişmiş audit field'ları
        private String requestMethod;
        private String requestUri;
        private String httpStatus;
        private String countryCode;
        private String regionName; // KVKK/GDPR: Only region name, not exact city
        private String deviceFingerprint;
        private String browserName;
        private String browserVersion;
        private String operatingSystem;
        private String apiKey;
        private String correlationId;
        private String requestHeaders;
        private Long requestSizeBytes;
        private Long responseSizeBytes;
        private Long processingTimeMs;
        
        private StringBuilder complianceTags = new StringBuilder();
        private StringBuilder additionalData = new StringBuilder("{");
        
        public AuditEventBuilder(AuditService auditService) {
            this.auditService = auditService;
        }
        
        public AuditEventBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public AuditEventBuilder severity(AuditLog.Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public AuditEventBuilder actor(String actor) {
            this.actor = actor;
            return this;
        }
        
        public AuditEventBuilder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }
        
        public AuditEventBuilder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }
        
        public AuditEventBuilder action(String action) {
            this.action = action;
            return this;
        }
        
        public AuditEventBuilder oldValues(Object oldValues) {
            this.oldValues = oldValues;
            return this;
        }
        
        public AuditEventBuilder newValues(Object newValues) {
            this.newValues = newValues;
            return this;
        }
        
        public AuditEventBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public AuditEventBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public AuditEventBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        // Yeni gelişmiş audit metodları
        public AuditEventBuilder requestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }
        
        public AuditEventBuilder requestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }
        
        public AuditEventBuilder httpStatus(String httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }
        
        public AuditEventBuilder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }
        
        public AuditEventBuilder regionName(String regionName) {
            this.regionName = regionName;
            return this;
        }
        
        public AuditEventBuilder deviceFingerprint(String deviceFingerprint) {
            this.deviceFingerprint = deviceFingerprint;
            return this;
        }
        
        public AuditEventBuilder browserName(String browserName) {
            this.browserName = browserName;
            return this;
        }
        
        public AuditEventBuilder browserVersion(String browserVersion) {
            this.browserVersion = browserVersion;
            return this;
        }
        
        public AuditEventBuilder operatingSystem(String operatingSystem) {
            this.operatingSystem = operatingSystem;
            return this;
        }
        
        public AuditEventBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
        
        public AuditEventBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public AuditEventBuilder requestHeaders(String requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }
        
        public AuditEventBuilder requestSizeBytes(Long requestSizeBytes) {
            this.requestSizeBytes = requestSizeBytes;
            return this;
        }
        
        public AuditEventBuilder responseSizeBytes(Long responseSizeBytes) {
            this.responseSizeBytes = responseSizeBytes;
            return this;
        }
        
        public AuditEventBuilder processingTimeMs(Long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }
        
        public AuditEventBuilder complianceTag(String tag) {
            if (complianceTags.length() > 0) {
                complianceTags.append(",");
            }
            complianceTags.append("\"").append(tag).append("\"");
            return this;
        }
        
        public AuditEventBuilder additionalData(String key, Object value) {
            if (!additionalData.toString().equals("{")) {
                additionalData.append(",");
            }
            additionalData.append("\"").append(key).append("\":");
            if (value instanceof String) {
                additionalData.append("\"").append(value).append("\"");
            } else {
                additionalData.append(value);
            }
            return this;
        }
        
        public AuditEventBuilder request(HttpServletRequest request) {
            if (request != null) {
                this.ipAddress = getClientIpAddress(request);
                this.userAgent = request.getHeader("User-Agent");
                this.sessionId = request.getSession(false) != null ? 
                    request.getSession(false).getId() : null;
            }
            return this;
        }
        
        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }
            
            return request.getRemoteAddr();
        }
        
        public void log() {
            auditService.logEvent(this);
        }
        
        AuditLog build() {
            additionalData.append("}");
            
            return AuditLog.builder()
                .eventType(eventType)
                .severity(severity)
                .actor(actor)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .action(action)
                .oldValues(auditService.toJson(oldValues))
                .newValues(auditService.toJson(newValues))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .sessionId(sessionId)
                .requestMethod(requestMethod)
                .requestUri(requestUri)
                .httpStatus(httpStatus)
                .countryCode(countryCode)
                .regionName(regionName) // KVKK/GDPR: Only region, not exact city
                .deviceFingerprint(deviceFingerprint)
                .browserName(browserName)
                .browserVersion(browserVersion)
                .operatingSystem(operatingSystem)
                .apiKey(apiKey)
                .correlationId(correlationId)
                .requestHeaders(requestHeaders)
                .requestSizeBytes(requestSizeBytes)
                .responseSizeBytes(responseSizeBytes)
                .processingTimeMs(processingTimeMs)
                .complianceTags("[" + complianceTags.toString() + "]")
                .additionalData(additionalData.toString())
                .build();
        }
    }
}
