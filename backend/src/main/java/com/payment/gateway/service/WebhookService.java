package com.payment.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.dto.WebhookDeliveryRequest;
import com.payment.gateway.dto.WebhookDeliveryResponse;
import com.payment.gateway.dto.WebhookRequest;
import com.payment.gateway.dto.WebhookResponse;
import com.payment.gateway.model.Webhook;
import com.payment.gateway.model.WebhookDelivery;
import com.payment.gateway.repository.WebhookDeliveryRepository;
import com.payment.gateway.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import com.payment.gateway.service.AuditService;
import com.payment.gateway.model.AuditLog;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookService {
    
    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    
    public WebhookResponse createWebhook(WebhookRequest request) {
        try {
            log.info("Creating webhook for merchant: {}, event: {}", request.getMerchantId(), request.getEventType());
            
            // Check if webhook already exists
            if (webhookRepository.existsByMerchantIdAndUrlAndEventType(
                    request.getMerchantId(), request.getUrl(), request.getEventType())) {
                return createErrorResponse("Webhook already exists for this merchant, URL, and event type");
            }
            
            // Generate unique webhook ID
            String webhookId = generateWebhookId();
            
            // Create webhook entity
            Webhook webhook = new Webhook();
            webhook.setWebhookId(webhookId);
            webhook.setMerchantId(request.getMerchantId());
            webhook.setUrl(request.getUrl());
            webhook.setEventType(request.getEventType());
            webhook.setSecretKey(request.getSecretKey());
            webhook.setStatus(Webhook.WebhookStatus.ACTIVE);
            webhook.setMaxRetries(request.getMaxRetries());
            webhook.setCurrentRetries(0);
            webhook.setTimeoutSeconds(request.getTimeoutSeconds());
            webhook.setDescription(request.getDescription());
            webhook.setIsActive(request.getIsActive());
            
            // Save to database
            Webhook savedWebhook = webhookRepository.save(webhook);
            
            // Audit logging
            auditService.createEvent()
                .eventType("WEBHOOK_CREATED")
                .severity(AuditLog.Severity.LOW)
                .actor("system")
                .action("CREATE")
                .resourceType("WEBHOOK")
                .resourceId(webhookId)
                .newValues(savedWebhook)
                .additionalData("merchantId", request.getMerchantId())
                .additionalData("eventType", request.getEventType())
                .additionalData("url", request.getUrl())
                .complianceTag("PCI_DSS")
                .log();
            
            log.info("Webhook created successfully with ID: {}", webhookId);
            return createWebhookResponse(savedWebhook, true, "Webhook created successfully");
            
        } catch (Exception e) {
            log.error("Error creating webhook: {}", e.getMessage(), e);
            return createErrorResponse("Failed to create webhook: " + e.getMessage());
        }
    }
    
    public WebhookResponse getWebhookById(Long id) {
        try {
            Optional<Webhook> webhook = webhookRepository.findById(id);
            if (webhook.isPresent()) {
                return createWebhookResponse(webhook.get(), true, "Webhook retrieved successfully");
            } else {
                return createErrorResponse("Webhook not found with ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error retrieving webhook by ID {}: {}", id, e.getMessage(), e);
            return createErrorResponse("Failed to retrieve webhook: " + e.getMessage());
        }
    }
    
    public WebhookResponse getWebhookByWebhookId(String webhookId) {
        try {
            Optional<Webhook> webhook = webhookRepository.findByWebhookId(webhookId);
            if (webhook.isPresent()) {
                return createWebhookResponse(webhook.get(), true, "Webhook retrieved successfully");
            } else {
                return createErrorResponse("Webhook not found with webhook ID: " + webhookId);
            }
        } catch (Exception e) {
            log.error("Error retrieving webhook by webhook ID {}: {}", webhookId, e.getMessage(), e);
            return createErrorResponse("Failed to retrieve webhook: " + e.getMessage());
        }
    }
    
    public List<WebhookResponse> getAllWebhooks() {
        try {
            List<Webhook> webhooks = webhookRepository.findAll();
            return webhooks.stream()
                    .map(webhook -> createWebhookResponse(webhook, true, "Webhook retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving all webhooks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve webhooks", e);
        }
    }
    
    public List<WebhookResponse> getWebhooksByMerchantId(String merchantId) {
        try {
            List<Webhook> webhooks = webhookRepository.findByMerchantId(merchantId);
            return webhooks.stream()
                    .map(webhook -> createWebhookResponse(webhook, true, "Webhook retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving webhooks for merchant {}: {}", merchantId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve webhooks for merchant", e);
        }
    }
    
    public List<WebhookResponse> getWebhooksByEventType(String eventType) {
        try {
            List<Webhook> webhooks = webhookRepository.findByEventType(eventType);
            return webhooks.stream()
                    .map(webhook -> createWebhookResponse(webhook, true, "Webhook retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving webhooks by event type {}: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve webhooks by event type", e);
        }
    }
    
    public List<WebhookResponse> getActiveWebhooks() {
        try {
            List<Webhook> webhooks = webhookRepository.findByIsActiveTrue();
            return webhooks.stream()
                    .map(webhook -> createWebhookResponse(webhook, true, "Webhook retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving active webhooks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active webhooks", e);
        }
    }
    
    public WebhookResponse updateWebhookStatus(Long id, Webhook.WebhookStatus status) {
        try {
            Optional<Webhook> webhookOpt = webhookRepository.findById(id);
            if (webhookOpt.isPresent()) {
                Webhook webhook = webhookOpt.get();
                webhook.setStatus(status);
                
                if (status == Webhook.WebhookStatus.SUSPENDED || status == Webhook.WebhookStatus.FAILED) {
                    webhook.setIsActive(false);
                } else if (status == Webhook.WebhookStatus.ACTIVE) {
                    webhook.setIsActive(true);
                }
                
                Webhook updatedWebhook = webhookRepository.save(webhook);
                log.info("Webhook status updated to {} for ID: {}", status, id);
                return createWebhookResponse(updatedWebhook, true, "Webhook status updated successfully");
            } else {
                return createErrorResponse("Webhook not found with ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error updating webhook status for ID {}: {}", id, e.getMessage(), e);
            return createErrorResponse("Failed to update webhook status: " + e.getMessage());
        }
    }
    
    public WebhookResponse deleteWebhook(Long id) {
        try {
            Optional<Webhook> webhookOpt = webhookRepository.findById(id);
            if (webhookOpt.isPresent()) {
                Webhook webhook = webhookOpt.get();
                webhook.setStatus(Webhook.WebhookStatus.DELETED);
                webhook.setIsActive(false);
                Webhook updatedWebhook = webhookRepository.save(webhook);
                log.info("Webhook deleted for ID: {}", id);
                return createWebhookResponse(updatedWebhook, true, "Webhook deleted successfully");
            } else {
                return createErrorResponse("Webhook not found with ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error deleting webhook for ID {}: {}", id, e.getMessage(), e);
            return createErrorResponse("Failed to delete webhook: " + e.getMessage());
        }
    }
    
    public void triggerWebhookDelivery(WebhookDeliveryRequest request) {
        try {
            log.info("Triggering webhook delivery for merchant: {}, event: {}", 
                    request.getMerchantId(), request.getEventType());
            
            // Find active webhooks for this merchant and event type
            List<Webhook> webhooks = webhookRepository.findActiveWebhooksByMerchantAndEvent(
                    request.getMerchantId(), request.getEventType());
            
            if (webhooks.isEmpty()) {
                log.warn("No active webhooks found for merchant: {} and event: {}", 
                        request.getMerchantId(), request.getEventType());
                return;
            }
            
            // Deliver to each webhook
            for (Webhook webhook : webhooks) {
                deliverWebhook(webhook, request);
            }
            
        } catch (Exception e) {
            log.error("Error triggering webhook delivery: {}", e.getMessage(), e);
        }
    }
    
    private void deliverWebhook(Webhook webhook, WebhookDeliveryRequest request) {
        try {
            String deliveryId = generateDeliveryId();
            
            // Create delivery record
            WebhookDelivery delivery = new WebhookDelivery();
            delivery.setDeliveryId(deliveryId);
            delivery.setWebhookId(webhook.getWebhookId());
            delivery.setMerchantId(request.getMerchantId());
            delivery.setEventType(request.getEventType());
            delivery.setEventData(objectMapper.writeValueAsString(request.getEventData()));
            delivery.setTargetUrl(webhook.getUrl());
            delivery.setStatus(WebhookDelivery.DeliveryStatus.PENDING);
            delivery.setAttemptNumber(1);
            
            // Save delivery record
            webhookDeliveryRepository.save(delivery);
            
            // Send webhook
            sendWebhook(webhook, delivery, request);
            
        } catch (Exception e) {
            log.error("Error delivering webhook: {}", e.getMessage(), e);
        }
    }
    
    private void sendWebhook(Webhook webhook, WebhookDelivery delivery, WebhookDeliveryRequest request) {
        try {
            LocalDateTime startTime = LocalDateTime.now();
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "PaymentGateway-Webhook/1.0");
            headers.set("X-Webhook-ID", webhook.getWebhookId());
            headers.set("X-Event-Type", request.getEventType());
            headers.set("X-Entity-ID", request.getEntityId());
            
            // Generate signature
            String payload = objectMapper.writeValueAsString(request.getEventData());
            String signature = generateSignature(payload, webhook.getSecretKey());
            headers.set("X-Signature", signature);
            
            // Create request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
            
            // Send webhook
            ResponseEntity<String> response = restTemplate.exchange(
                    webhook.getUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            // Update delivery record
            delivery.setStatus(WebhookDelivery.DeliveryStatus.DELIVERED);
            delivery.setResponseCode(response.getStatusCode().value());
            delivery.setResponseBody(response.getBody());
            delivery.setSentAt(startTime);
            delivery.setReceivedAt(LocalDateTime.now());
            delivery.setResponseTimeMs((int) java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());
            delivery.setHeaders(objectMapper.writeValueAsString(headers));
            
            webhookDeliveryRepository.save(delivery);
            
            log.info("Webhook delivered successfully: {}", delivery.getDeliveryId());
            
        } catch (ResourceAccessException e) {
            handleDeliveryFailure(webhook, delivery, "Connection timeout: " + e.getMessage());
        } catch (Exception e) {
            handleDeliveryFailure(webhook, delivery, "Delivery failed: " + e.getMessage());
        }
    }
    
    private void handleDeliveryFailure(Webhook webhook, WebhookDelivery delivery, String errorMessage) {
        try {
            delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
            delivery.setErrorMessage(errorMessage);
            delivery.setSentAt(LocalDateTime.now());
            
            webhookDeliveryRepository.save(delivery);
            
            // Update webhook retry count
            webhook.setCurrentRetries(webhook.getCurrentRetries() + 1);
            // Bu field'lar Webhook model'inde mevcut değil, sadece status güncelleniyor
            
            if (webhook.getCurrentRetries() >= webhook.getMaxRetries()) {
                webhook.setStatus(Webhook.WebhookStatus.FAILED);
                webhook.setIsActive(false);
            } else {
                // Retry scheduled - delivery status güncelleniyor
                delivery.setStatus(WebhookDelivery.DeliveryStatus.RETRY_SCHEDULED);
            }
            
            webhookRepository.save(webhook);
            webhookDeliveryRepository.save(delivery);
            
            log.warn("Webhook delivery failed: {} - {}", delivery.getDeliveryId(), errorMessage);
            
        } catch (Exception e) {
            log.error("Error handling delivery failure: {}", e.getMessage(), e);
        }
    }
    
    private String generateSignature(String payload, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return java.util.Base64.getEncoder().encodeToString(signature);
    }
    
    public List<WebhookDeliveryResponse> getDeliveriesByWebhookId(String webhookId) {
        try {
            List<WebhookDelivery> deliveries = webhookDeliveryRepository.findByWebhookId(webhookId);
            return deliveries.stream()
                    .map(this::createDeliveryResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving deliveries for webhook {}: {}", webhookId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve deliveries", e);
        }
    }
    
    public List<WebhookDeliveryResponse> getDeliveriesByMerchantId(String merchantId) {
        try {
            List<WebhookDelivery> deliveries = webhookDeliveryRepository.findByMerchantId(merchantId);
            return deliveries.stream()
                    .map(this::createDeliveryResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving deliveries for merchant {}: {}", merchantId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve deliveries", e);
        }
    }
    
    public void processRetries() {
        try {
            // Bu metod şimdilik basitleştirildi - tüm active webhook'ları kontrol et
            List<Webhook> activeWebhooks = webhookRepository.findByIsActiveTrue();
            
            for (Webhook webhook : activeWebhooks) {
                log.info("Processing retry for webhook: {}", webhook.getWebhookId());
                
                // Find the last failed delivery
                List<WebhookDelivery> failedDeliveries = webhookDeliveryRepository.findByWebhookId(webhook.getWebhookId());
                WebhookDelivery lastDelivery = failedDeliveries.stream()
                        .filter(d -> d.getStatus() == WebhookDelivery.DeliveryStatus.FAILED)
                        .max((d1, d2) -> d1.getCreatedAt().compareTo(d2.getCreatedAt()))
                        .orElse(null);
                
                if (lastDelivery != null) {
                    // Create new delivery attempt
                    WebhookDelivery newDelivery = new WebhookDelivery();
                    newDelivery.setDeliveryId(generateDeliveryId());
                    newDelivery.setWebhookId(webhook.getWebhookId());
                    newDelivery.setMerchantId(lastDelivery.getMerchantId());
                    newDelivery.setEventType(lastDelivery.getEventType());
                    newDelivery.setEventData(lastDelivery.getEventData());
                    newDelivery.setTargetUrl(lastDelivery.getTargetUrl());
                    newDelivery.setStatus(WebhookDelivery.DeliveryStatus.PENDING);
                    newDelivery.setAttemptNumber(lastDelivery.getAttemptNumber() + 1);
                    
                    webhookDeliveryRepository.save(newDelivery);
                    
                    // Retry delivery
                    try {
                        // Recreate delivery request
                        WebhookDeliveryRequest retryRequest = new WebhookDeliveryRequest();
                        retryRequest.setMerchantId(lastDelivery.getMerchantId());
                        retryRequest.setEventType(lastDelivery.getEventType());
                        retryRequest.setEventData(objectMapper.readValue(lastDelivery.getEventData(), Object.class));
                        retryRequest.setEntityId("retry-" + lastDelivery.getDeliveryId());
                        
                        sendWebhook(webhook, newDelivery, retryRequest);
                        
                    } catch (Exception e) {
                        log.error("Error processing retry for webhook {}: {}", webhook.getWebhookId(), e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing webhook retries: {}", e.getMessage(), e);
        }
    }
    
    // Helper methods
    private String generateWebhookId() {
        return "WH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateDeliveryId() {
        return "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private WebhookResponse createWebhookResponse(Webhook webhook, boolean success, String message) {
        WebhookResponse response = new WebhookResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setWebhookId(webhook.getWebhookId());
        response.setMerchantId(webhook.getMerchantId());
        response.setUrl(webhook.getUrl());
        response.setEventType(webhook.getEventType());
        response.setStatus(webhook.getStatus());
        response.setMaxRetries(webhook.getMaxRetries());
        response.setCurrentRetries(webhook.getCurrentRetries());
        response.setTimeoutSeconds(webhook.getTimeoutSeconds());
        // Bu field'lar Webhook model'inde mevcut değil, null olarak bırakılıyor
        response.setLastAttemptAt(null);
        response.setNextAttemptAt(null);
        response.setLastError(null);
        response.setLastResponse(null);
        response.setLastResponseCode(null);
        response.setDescription(webhook.getDescription());
        response.setIsActive(webhook.getIsActive());
        response.setCreatedAt(webhook.getCreatedAt());
        response.setUpdatedAt(webhook.getUpdatedAt());
        return response;
    }
    
    private WebhookResponse createErrorResponse(String message) {
        WebhookResponse response = new WebhookResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    private WebhookDeliveryResponse createDeliveryResponse(WebhookDelivery delivery) {
        WebhookDeliveryResponse response = new WebhookDeliveryResponse();
        response.setSuccess(delivery.getStatus() == WebhookDelivery.DeliveryStatus.DELIVERED);
        response.setMessage("Delivery " + delivery.getStatus().toString().toLowerCase());
        response.setDeliveryId(delivery.getDeliveryId());
        response.setWebhookId(delivery.getWebhookId());
        response.setMerchantId(delivery.getMerchantId());
        response.setEventType(delivery.getEventType());
        response.setTargetUrl(delivery.getTargetUrl());
        response.setStatus(delivery.getStatus());
        response.setAttemptNumber(delivery.getAttemptNumber());
        response.setResponseCode(delivery.getResponseCode());
        response.setResponseBody(delivery.getResponseBody());
        response.setErrorMessage(delivery.getErrorMessage());
        response.setSentAt(delivery.getSentAt());
        response.setReceivedAt(delivery.getReceivedAt());
        response.setResponseTimeMs(delivery.getResponseTimeMs());
        response.setHeaders(delivery.getHeaders());
        response.setCreatedAt(delivery.getCreatedAt());
        return response;
    }
}
