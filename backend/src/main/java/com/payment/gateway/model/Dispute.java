package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dispute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String disputeId;
    
    @Column(nullable = false)
    private String paymentId;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeReason reason;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 1000)
    private String evidence;
    
    @Column(length = 1000)
    private String gatewayResponse;
    
    @Column(length = 100)
    private String gatewayDisputeId;
    
    @Column(nullable = false)
    private LocalDateTime disputeDate;
    
    @Column
    private LocalDateTime resolutionDate;
    
    // Yeni alanlar - Bank Dispute Management
    @Column(length = 100)
    private String bankDisputeId;
    
    @Column(length = 2000)
    private String bankNotificationData;
    
    @Column
    private LocalDateTime merchantResponseDeadline;
    
    @Column(length = 20)
    private String merchantResponse; // ACCEPT, DEFEND, AUTO_ACCEPTED_EXPIRED
    
    @Column(length = 3000)
    private String merchantDefenseEvidence;
    
    @Column
    private LocalDateTime merchantResponseDate;
    
    @Column(length = 2000)
    private String adminEvaluation;
    
    @Column(length = 50)
    private String adminDecision; // APPROVE_MERCHANT, APPROVE_CUSTOMER, PARTIAL_REFUND
    
    @Column(length = 50)
    private String bankFinalDecision; // MERCHANT_APPROVED, CUSTOMER_APPROVED
    
    @Column(precision = 10, scale = 2)
    private BigDecimal chargebackAmount;
    
    @Column(length = 50)
    private String bankName; // GARANTI_BBVA, ISBANK, YAPIKREDI
    
    @Column
    private LocalDateTime adminEvaluationDeadline; // Admin evaluation deadline
    
    @Column(length = 2000)
    private String adminNotes; // Admin notları
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DisputeStatus {
        OPENED, UNDER_REVIEW, EVIDENCE_REQUIRED, RESOLVED, CLOSED, WON, LOST, PARTIAL_REFUND,
        // Yeni bank-initiated dispute statuses
        BANK_INITIATED,                 // Banka tarafından başlatıldı
        MERCHANT_NOTIFIED,              // Merchant'a bildirim gönderildi 
        AWAITING_MERCHANT_RESPONSE,     // Merchant cevabı bekleniyor
        PENDING_MERCHANT_RESPONSE,      // Merchant response bekleniyor (scheduler için)
        MERCHANT_ACCEPTED,              // Merchant kabul etti
        MERCHANT_DEFENDED,              // Merchant savunma yaptı
        ADMIN_EVALUATING,               // Admin değerlendiriyor
        PENDING_ADMIN_EVALUATION,       // Admin evaluation bekleniyor (scheduler için)
        BANK_DECISION_PENDING,          // Banka kararı bekleniyor
        BANK_APPROVED,                  // Banka merchant'ı onayladı
        BANK_REJECTED                   // Banka customer'ı onayladı
    }
    
    public enum DisputeReason {
        FRAUD, DUPLICATE, PRODUCT_NOT_RECEIVED, PRODUCT_NOT_AS_DESCRIBED, 
        CREDIT_NOT_PROCESSED, GENERAL, OTHER,
        // Yeni bank dispute reasons
        UNAUTHORIZED_TRANSACTION,       // Yetkisiz işlem
        NON_RECEIPT,                   // Ürün alınmadı
        DEFECTIVE_PRODUCT,             // Hatalı ürün
        SUBSCRIPTION_CANCELLED,        // Abonelik iptal edildi
        DUPLICATE_CHARGE,              // Çift ücretlendirme
        PROCESSING_ERROR               // İşlem hatası
    }
}