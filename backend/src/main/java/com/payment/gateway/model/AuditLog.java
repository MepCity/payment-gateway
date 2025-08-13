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
        // Authentication Events
        LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT,
        API_KEY_CREATED, API_KEY_REVOKED,
        
        // Payment Events
        PAYMENT_INITIATED, PAYMENT_COMPLETED, PAYMENT_FAILED,
        REFUND_INITIATED, REFUND_COMPLETED, REFUND_FAILED,
        
        // Blacklist Events
        BLACKLIST_ENTRY_ADDED, BLACKLIST_ENTRY_REMOVED,
        BLACKLIST_CHECK_PERFORMED,
        
        // Risk Assessment Events
        RISK_ASSESSMENT_PERFORMED, FRAUD_DETECTED,
        VELOCITY_LIMIT_EXCEEDED,
        
        // Administrative Events
        CONFIGURATION_CHANGED, USER_CREATED, USER_DELETED,
        SYSTEM_MAINTENANCE, DATA_EXPORT,
        
        // Security Events
        UNAUTHORIZED_ACCESS_ATTEMPT, SUSPICIOUS_ACTIVITY,
        SECURITY_POLICY_VIOLATION, DATA_BREACH_ATTEMPT
    }
}
