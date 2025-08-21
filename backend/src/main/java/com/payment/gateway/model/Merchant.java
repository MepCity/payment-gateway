package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "merchant_id", nullable = false, unique = true)
    private String merchantId;
    
    @Column(name = "merchant_name")
    private String name;
    
    @Column
    private String email;
    
    @Column
    private String password;
    
    @Column
    private String phone;
    
    @Column
    private String address;
    
    @Column
    private String website;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantStatus status = MerchantStatus.ACTIVE;
    
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "secret_key")
    private String secretKey;
    
    @Column(name = "webhook_url")
    private String webhookUrl;
    
    @Column(name = "webhook_events", columnDefinition = "jsonb")
    private String webhookEvents;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum MerchantStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}