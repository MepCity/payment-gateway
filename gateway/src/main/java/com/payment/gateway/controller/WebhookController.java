package com.payment.gateway.controller;

import com.payment.gateway.dto.WebhookDeliveryRequest;
import com.payment.gateway.dto.WebhookDeliveryResponse;
import com.payment.gateway.dto.WebhookRequest;
import com.payment.gateway.dto.WebhookResponse;
import com.payment.gateway.model.Webhook;
import com.payment.gateway.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WebhookController {
    
    private final WebhookService webhookService;
    
    @PostMapping("/")
    public ResponseEntity<WebhookResponse> createWebhook(@Valid @RequestBody WebhookRequest request) {
        log.info("Received webhook creation request for merchant: {}", request.getMerchantId());
        WebhookResponse response = webhookService.createWebhook(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WebhookResponse> getWebhookById(@PathVariable Long id) {
        log.info("Retrieving webhook by ID: {}", id);
        WebhookResponse response = webhookService.getWebhookById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/webhook-id/{webhookId}")
    public ResponseEntity<WebhookResponse> getWebhookByWebhookId(@PathVariable String webhookId) {
        log.info("Retrieving webhook by webhook ID: {}", webhookId);
        WebhookResponse response = webhookService.getWebhookByWebhookId(webhookId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/")
    public ResponseEntity<List<WebhookResponse>> getAllWebhooks() {
        log.info("Retrieving all webhooks");
        List<WebhookResponse> webhooks = webhookService.getAllWebhooks();
        return ResponseEntity.ok(webhooks);
    }
    
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<WebhookResponse>> getWebhooksByMerchantId(@PathVariable String merchantId) {
        log.info("Retrieving webhooks for merchant: {}", merchantId);
        List<WebhookResponse> webhooks = webhookService.getWebhooksByMerchantId(merchantId);
        return ResponseEntity.ok(webhooks);
    }
    
    @GetMapping("/event/{eventType}")
    public ResponseEntity<List<WebhookResponse>> getWebhooksByEventType(@PathVariable String eventType) {
        log.info("Retrieving webhooks by event type: {}", eventType);
        List<WebhookResponse> webhooks = webhookService.getWebhooksByEventType(eventType);
        return ResponseEntity.ok(webhooks);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<WebhookResponse>> getActiveWebhooks() {
        log.info("Retrieving active webhooks");
        List<WebhookResponse> webhooks = webhookService.getActiveWebhooks();
        return ResponseEntity.ok(webhooks);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<WebhookResponse> updateWebhookStatus(
            @PathVariable Long id,
            @RequestParam Webhook.WebhookStatus status) {
        log.info("Updating webhook status for ID: {} to: {}", id, status);
        WebhookResponse response = webhookService.updateWebhookStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<WebhookResponse> deleteWebhook(@PathVariable Long id) {
        log.info("Deleting webhook with ID: {}", id);
        WebhookResponse response = webhookService.deleteWebhook(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/deliver")
    public ResponseEntity<Void> triggerWebhookDelivery(@Valid @RequestBody WebhookDeliveryRequest request) {
        log.info("Triggering webhook delivery for merchant: {}, event: {}", 
                request.getMerchantId(), request.getEventType());
        
        try {
            webhookService.triggerWebhookDelivery(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error triggering webhook delivery: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{webhookId}/deliveries")
    public ResponseEntity<List<WebhookDeliveryResponse>> getDeliveriesByWebhookId(@PathVariable String webhookId) {
        log.info("Retrieving deliveries for webhook: {}", webhookId);
        List<WebhookDeliveryResponse> deliveries = webhookService.getDeliveriesByWebhookId(webhookId);
        return ResponseEntity.ok(deliveries);
    }
    
    @GetMapping("/merchant/{merchantId}/deliveries")
    public ResponseEntity<List<WebhookDeliveryResponse>> getDeliveriesByMerchantId(@PathVariable String merchantId) {
        log.info("Retrieving deliveries for merchant: {}", merchantId);
        List<WebhookDeliveryResponse> deliveries = webhookService.getDeliveriesByMerchantId(merchantId);
        return ResponseEntity.ok(deliveries);
    }
    
    @PostMapping("/retries/process")
    public ResponseEntity<Void> processRetries() {
        log.info("Processing webhook retries");
        
        try {
            webhookService.processRetries();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing webhook retries: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook service is healthy");
    }
}
