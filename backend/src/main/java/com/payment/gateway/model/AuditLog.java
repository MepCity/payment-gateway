package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_event_type", columnList = "eventType"),
    @Index(name = "idx_audit_actor", columnList = "actor"),
    @Index(name = "idx_audit_resource", columnList = "resourceType, resourceId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false, length = 100)
    private String eventType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    
    @Column(nullable = false, length = 100)
    private String actor; // user, system, api-client
    
    @Column(length = 100)
    private String resourceType; // PAYMENT, BLACKLIST, CUSTOMER, etc.
    
    @Column(length = 200)
    private String resourceId;
    
    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, VIEW
    
    @Column(columnDefinition = "TEXT")
    private String oldValues; // JSON string
    
    @Column(columnDefinition = "TEXT")
    private String newValues; // JSON string
    
    @Column(length = 45)
    private String ipAddress;
    
    @Column(columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(length = 200)
    private String sessionId;
    
    // Gelişmiş güvenlik ve lokasyon bilgileri
    @Column(length = 100)
    private String requestMethod; // GET, POST, DELETE, etc.
    
    @Column(length = 500)
    private String requestUri; // /api/v1/blacklist/add
    
    @Column(length = 100)
    private String httpStatus; // 200, 400, 500, etc.
    
    @Column(length = 100)
    private String countryCode; // TR, US, etc. (from IP geolocation)
    
    @Column(length = 100)
    private String regionName; // Marmara, California, etc. (region level only for privacy)
    
    // KVKK/GDPR Note: City data is anonymized to region level
    // No exact coordinates stored to protect user privacy
    
    @Column(length = 200)
    private String deviceFingerprint; // Unique device identifier
    
    @Column(length = 100)
    private String browserName; // Chrome, Firefox, Safari
    
    @Column(length = 50)
    private String browserVersion; // 91.0.4472.124
    
    @Column(length = 100)
    private String operatingSystem; // Windows 10, macOS, Android
    
    @Column(length = 100)
    private String apiKey; // Masked API key for tracking
    
    @Column(length = 100)
    private String correlationId; // Request tracking ID
    
    @Column(columnDefinition = "TEXT")
    private String requestHeaders; // Important headers as JSON
    
    @Column
    private Long requestSizeBytes; // Request payload size
    
    @Column
    private Long responseSizeBytes; // Response payload size
    
    @Column
    private Long processingTimeMs; // Processing duration
    
    @Column(columnDefinition = "TEXT")
    private String complianceTags; // JSON array string: ["PCI_DSS", "GDPR"]
    
    @Column
    private LocalDateTime retentionUntil;
    
    @Column(columnDefinition = "TEXT")
    private String additionalData; // JSON string for extra context
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (retentionUntil == null) {
            // Default 7 years retention for audit logs
            retentionUntil = timestamp.plusYears(7);
        }
    }
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum EventType {
        // Authentication & Authorization Events
        LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, SESSION_TIMEOUT,
        API_KEY_CREATED, API_KEY_REVOKED, API_KEY_USED,
        TOKEN_GENERATED, TOKEN_EXPIRED, TOKEN_REVOKED,
        MULTI_FACTOR_AUTH_SUCCESS, MULTI_FACTOR_AUTH_FAILURE,
        PASSWORD_CHANGED, PASSWORD_RESET_REQUESTED,
        
        // Payment Processing Events
        PAYMENT_INITIATED, PAYMENT_AUTHORIZED, PAYMENT_CAPTURED,
        PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_CANCELLED,
        PAYMENT_REVERSED, PAYMENT_SETTLED,
        REFUND_INITIATED, REFUND_COMPLETED, REFUND_FAILED,
        CHARGEBACK_RECEIVED, CHARGEBACK_DISPUTED,
        
        // Card & Account Events
        CARD_VALIDATION_SUCCESS, CARD_VALIDATION_FAILURE,
        CVV_VERIFICATION_SUCCESS, CVV_VERIFICATION_FAILURE,
        THREE_D_SECURE_INITIATED, THREE_D_SECURE_COMPLETED,
        THREE_D_SECURE_FAILED, BIN_LOOKUP_PERFORMED,
        
        // Blacklist & Fraud Events
        BLACKLIST_ENTRY_ADDED, BLACKLIST_ENTRY_REMOVED,
        BLACKLIST_CHECK_PERFORMED, BLACKLIST_HIT_DETECTED,
        FRAUD_RULE_TRIGGERED, FRAUD_SCORE_CALCULATED,
        RISK_ASSESSMENT_PERFORMED, FRAUD_DETECTED,
        VELOCITY_LIMIT_EXCEEDED, SUSPICIOUS_PATTERN_DETECTED,
        
        // Merchant & Customer Events
        MERCHANT_ONBOARDED, MERCHANT_SUSPENDED, MERCHANT_REACTIVATED,
        CUSTOMER_CREATED, CUSTOMER_UPDATED, CUSTOMER_BLOCKED,
        KYC_VERIFICATION_INITIATED, KYC_VERIFICATION_COMPLETED,
        
        // Transaction Monitoring Events
        TRANSACTION_MONITORING_ALERT, AML_CHECK_PERFORMED,
        SANCTIONS_SCREENING_PERFORMED, PEP_CHECK_PERFORMED,
        LARGE_TRANSACTION_REPORTED, SUSPICIOUS_ACTIVITY_REPORTED,
        
        // Administrative Events
        CONFIGURATION_CHANGED, SYSTEM_PARAMETER_UPDATED,
        USER_CREATED, USER_DELETED, USER_ROLE_CHANGED,
        SYSTEM_MAINTENANCE, DATABASE_BACKUP_COMPLETED,
        DATA_EXPORT, AUDIT_LOG_ACCESSED, REPORT_GENERATED,
        
        // Security Events
        UNAUTHORIZED_ACCESS_ATTEMPT, BRUTE_FORCE_DETECTED,
        IP_ADDRESS_BLOCKED, SUSPICIOUS_ACTIVITY,
        SECURITY_POLICY_VIOLATION, DATA_BREACH_ATTEMPT,
        UNUSUAL_LOGIN_LOCATION, DEVICE_CHANGE_DETECTED,
        
        // Compliance Events
        PCI_AUDIT_LOG_CREATED, GDPR_DATA_REQUEST,
        DATA_RETENTION_POLICY_APPLIED, COMPLIANCE_REPORT_GENERATED,
        REGULATORY_SUBMISSION, AUDIT_TRAIL_REVIEWED,
        
        // System Events
        SYSTEM_STARTUP, SYSTEM_SHUTDOWN, SERVICE_DEGRADATION,
        ERROR_THRESHOLD_EXCEEDED, RATE_LIMIT_EXCEEDED,
        DATABASE_CONNECTION_FAILURE, EXTERNAL_SERVICE_FAILURE
    }
}
