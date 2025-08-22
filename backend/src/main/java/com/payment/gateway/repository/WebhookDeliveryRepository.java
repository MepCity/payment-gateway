package com.payment.gateway.repository;

import com.payment.gateway.model.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    
    Optional<WebhookDelivery> findByDeliveryId(String deliveryId);
    
    List<WebhookDelivery> findByWebhookId(String webhookId);
    
    List<WebhookDelivery> findByMerchantId(String merchantId);
    
    List<WebhookDelivery> findByEventType(String eventType);
    
    List<WebhookDelivery> findByStatus(WebhookDelivery.DeliveryStatus status);
    
    List<WebhookDelivery> findByMerchantIdAndEventType(String merchantId, String eventType);
    
    List<WebhookDelivery> findByMerchantIdAndStatus(String merchantId, WebhookDelivery.DeliveryStatus status);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.createdAt BETWEEN :startDate AND :endDate")
    List<WebhookDelivery> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.merchantId = :merchantId AND wd.createdAt BETWEEN :startDate AND :endDate")
    List<WebhookDelivery> findByMerchantIdAndDateRange(@Param("merchantId") String merchantId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhookId = :webhookId AND wd.createdAt BETWEEN :startDate AND :endDate")
    List<WebhookDelivery> findByWebhookIdAndDateRange(@Param("webhookId") String webhookId, 
                                                      @Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.merchantId = :merchantId AND wd.status = :status")
    long countByMerchantIdAndStatus(@Param("merchantId") String merchantId, @Param("status") WebhookDelivery.DeliveryStatus status);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.eventType = :eventType AND wd.status = :status")
    long countByEventTypeAndStatus(@Param("eventType") String eventType, @Param("status") WebhookDelivery.DeliveryStatus status);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.webhookId = :webhookId AND wd.status = :status")
    long countByWebhookIdAndStatus(@Param("webhookId") String webhookId, @Param("status") WebhookDelivery.DeliveryStatus status);
    
    @Query("SELECT AVG(wd.responseTimeMs) FROM WebhookDelivery wd WHERE wd.webhookId = :webhookId AND wd.status = 'DELIVERED'")
    Double getAverageResponseTimeByWebhookId(@Param("webhookId") String webhookId);
    
    @Query("SELECT AVG(wd.responseTimeMs) FROM WebhookDelivery wd WHERE wd.merchantId = :merchantId AND wd.status = 'DELIVERED'")
    Double getAverageResponseTimeByMerchantId(@Param("merchantId") String merchantId);
    
    boolean existsByDeliveryId(String deliveryId);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.responseCode >= 400")
    List<WebhookDelivery> findFailedDeliveries();
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.merchantId = :merchantId AND wd.responseCode >= 400")
    List<WebhookDelivery> findFailedDeliveriesByMerchantId(@Param("merchantId") String merchantId);
}