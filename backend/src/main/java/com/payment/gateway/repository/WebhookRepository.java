package com.payment.gateway.repository;

import com.payment.gateway.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    
    Optional<Webhook> findByWebhookId(String webhookId);
    
    List<Webhook> findByMerchantId(String merchantId);
    
    List<Webhook> findByEventType(String eventType);
    
    List<Webhook> findByStatus(Webhook.WebhookStatus status);
    
    List<Webhook> findByMerchantIdAndEventType(String merchantId, String eventType);
    
    List<Webhook> findByMerchantIdAndStatus(String merchantId, Webhook.WebhookStatus status);
    
    List<Webhook> findByIsActiveTrue();
    
    List<Webhook> findByIsActiveTrueAndStatus(Webhook.WebhookStatus status);
    
    List<Webhook> findByMerchantIdAndIsActiveTrue(String merchantId);
    
    @Query("SELECT w FROM Webhook w WHERE w.nextAttemptAt <= :now AND w.currentRetries < w.maxRetries AND w.status = 'ACTIVE'")
    List<Webhook> findPendingRetries(@Param("now") LocalDateTime now);
    
    @Query("SELECT w FROM Webhook w WHERE w.merchantId = :merchantId AND w.eventType = :eventType AND w.isActive = true")
    List<Webhook> findActiveWebhooksByMerchantAndEvent(@Param("merchantId") String merchantId, 
                                                       @Param("eventType") String eventType);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.merchantId = :merchantId AND w.status = :status")
    long countByMerchantIdAndStatus(@Param("merchantId") String merchantId, @Param("status") Webhook.WebhookStatus status);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.eventType = :eventType AND w.status = :status")
    long countByEventTypeAndStatus(@Param("eventType") String eventType, @Param("status") Webhook.WebhookStatus status);
    
    boolean existsByWebhookId(String webhookId);
    
    boolean existsByMerchantIdAndUrlAndEventType(String merchantId, String url, String eventType);
    
    @Query("SELECT w FROM Webhook w WHERE w.url LIKE %:url%")
    List<Webhook> findByUrlContaining(@Param("url") String url);
    
    @Query("SELECT w FROM Webhook w WHERE w.description LIKE %:description%")
    List<Webhook> findByDescriptionContaining(@Param("description") String description);
}