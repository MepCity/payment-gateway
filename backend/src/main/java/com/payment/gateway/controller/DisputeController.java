package com.payment.gateway.controller;

import com.payment.gateway.dto.DisputeRequest;
import com.payment.gateway.dto.DisputeResponse;
import com.payment.gateway.model.Dispute;
import com.payment.gateway.service.DisputeService;
import com.payment.gateway.service.MerchantAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/disputes")
@RequiredArgsConstructor
@Slf4j
public class DisputeController {
    
    private final DisputeService disputeService;
    private final MerchantAuthService merchantAuthService;

    // POST - Create new dispute
    @PostMapping
    public ResponseEntity<DisputeResponse> createDispute(
            @Valid @RequestBody DisputeRequest request,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Creating new dispute for payment: {}, transaction: {}", 
                request.getPaymentId(), request.getTransactionId());

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile dispute create denemesi");
            DisputeResponse errorResponse = new DisputeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Geçersiz API key. Lütfen doğru API key kullanın.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Merchant ID'yi API key'den al
        String merchantId = getMerchantIdFromApiKey(apiKey);
        if (merchantId == null) {
            DisputeResponse errorResponse = new DisputeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Merchant bilgisi alınamadı.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        DisputeResponse response = disputeService.createDisputeForMerchant(request, merchantId);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // GET - Get dispute by ID
    @GetMapping("/{id}")
    public ResponseEntity<DisputeResponse> getDisputeById(
            @PathVariable Long id,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Retrieving dispute with ID: {}", id);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile dispute get denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant ID'yi API key'den al
        String merchantId = getMerchantIdFromApiKey(apiKey);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DisputeResponse response = disputeService.getDisputeByIdForMerchant(id, merchantId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get dispute by dispute ID (merchant-specific)
    @GetMapping("/dispute-id/{disputeId}")
    public ResponseEntity<DisputeResponse> getDisputeByDisputeId(
            @PathVariable String disputeId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Retrieving dispute with dispute ID: {}", disputeId);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile dispute by dispute ID denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant ID'yi API key'den al
        String merchantId = getMerchantIdFromApiKey(apiKey);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return disputeService.getDisputeForMerchantById(merchantId, disputeId)
                .map(dispute -> ResponseEntity.ok(dispute))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // GET - Get dispute by payment ID (merchant-specific)
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<DisputeResponse> getDisputeByPaymentId(
            @PathVariable String paymentId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Retrieving dispute with payment ID: {}", paymentId);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile dispute by payment ID denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant ID'yi API key'den al
        String merchantId = getMerchantIdFromApiKey(apiKey);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return disputeService.getDisputeForMerchantByPaymentId(merchantId, paymentId)
                .map(dispute -> ResponseEntity.ok(dispute))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // GET - Get all disputes for merchant
    @GetMapping
    public ResponseEntity<List<DisputeResponse>> getAllDisputes(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Retrieving all disputes for merchant");

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile disputes list denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant ID'yi API key'den al
        String merchantId = getMerchantIdFromApiKey(apiKey);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<DisputeResponse> disputes = disputeService.getDisputesByMerchantId(merchantId);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by merchant ID (for admin use, requires merchant authentication)
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByMerchantId(
            @PathVariable String merchantId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Retrieving disputes for merchant: {}", merchantId);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile merchant disputes denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant sadece kendi dispute'larını görebilir
        String requestingMerchantId = getMerchantIdFromApiKey(apiKey);
        if (requestingMerchantId == null || !requestingMerchantId.equals(merchantId)) {
            log.warn("🚫 Merchant {} tried to access disputes of {}", requestingMerchantId, merchantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DisputeResponse> disputes = disputeService.getDisputesByMerchantId(merchantId);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByCustomerId(@PathVariable String customerId) {
        log.info("Retrieving disputes for customer: {}", customerId);
        
        List<DisputeResponse> disputes = disputeService.getDisputesByCustomerId(customerId);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by status (merchant-specific)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByStatus(
            @PathVariable Dispute.DisputeStatus status,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Retrieving disputes with status: {}", status);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile disputes by status denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant ID'yi API key'den al
        String merchantId = getMerchantIdFromApiKey(apiKey);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<DisputeResponse> disputes = disputeService.getDisputesByStatus(status);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by reason (merchant-specific)
    @GetMapping("/reason/{reason}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByReason(
            @PathVariable Dispute.DisputeReason reason,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("Retrieving disputes with reason: {}", reason);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile disputes by reason denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant ID'yi API key'den al
        String merchantId = getMerchantIdFromApiKey(apiKey);
        if (merchantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<DisputeResponse> disputes = disputeService.getDisputesForMerchantByReason(merchantId, reason);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByTransactionId(@PathVariable String transactionId) {
        log.info("Retrieving disputes for transaction: {}", transactionId);
        
        List<DisputeResponse> disputes = disputeService.getDisputesByTransactionId(transactionId);
        return ResponseEntity.ok(disputes);
    }
    
    // POST - Update dispute
    @PostMapping("/{id}/update")
    public ResponseEntity<DisputeResponse> updateDispute(@PathVariable Long id, @Valid @RequestBody DisputeRequest request) {
        log.info("Updating dispute with ID: {}", id);
        
        DisputeResponse response = disputeService.updateDispute(id, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Add evidence to dispute (Merchant kanıt gönderir)
    @PostMapping("/{id}/evidence")
    public ResponseEntity<DisputeResponse> addEvidenceToDispute(
            @PathVariable Long id,
            @RequestBody Map<String, String> evidenceRequest) {
        log.info("Adding evidence to dispute with ID: {}", id);
        
        String evidence = evidenceRequest.get("evidence");
        String additionalNotes = evidenceRequest.get("additionalNotes");
        
        DisputeResponse response = disputeService.addEvidenceToDispute(id, evidence, additionalNotes);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Evaluate dispute (Admin değerlendirir)
    @PostMapping("/{id}/evaluate")
    public ResponseEntity<DisputeResponse> evaluateDispute(
            @PathVariable Long id,
            @RequestBody Map<String, String> evaluationRequest) {
        log.info("Evaluating dispute with ID: {}", id);
        
        String decision = evaluationRequest.get("decision"); // APPROVED, REJECTED, PARTIAL_REFUND
        String adminNotes = evaluationRequest.get("adminNotes");
        String refundAmount = evaluationRequest.get("refundAmount"); // Eğer partial refund ise
        
        DisputeResponse response = disputeService.evaluateDispute(id, decision, adminNotes, refundAmount);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Notify merchant about dispute result
    @PostMapping("/{id}/notify-merchant")
    public ResponseEntity<DisputeResponse> notifyMerchantAboutDisputeResult(@PathVariable Long id) {
        log.info("Notifying merchant about dispute result for ID: {}", id);
        
        DisputeResponse response = disputeService.notifyMerchantAboutDisputeResult(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // PUT - Update dispute status
    @PutMapping("/{id}/status")
    public ResponseEntity<DisputeResponse> updateDisputeStatus(
            @PathVariable Long id, 
            @RequestParam Dispute.DisputeStatus status) {
        log.info("Updating dispute status to {} for ID: {}", status, id);
        
        DisputeResponse response = disputeService.updateDisputeStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Close dispute
    @PostMapping("/{id}/close")
    public ResponseEntity<DisputeResponse> closeDispute(@PathVariable Long id) {
        log.info("Closing dispute with ID: {}", id);
        
        DisputeResponse response = disputeService.closeDispute(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API key'den merchant ID'yi çıkart
     */
    private String getMerchantIdFromApiKey(String apiKey) {
        if (apiKey == null) {
            return null;
        }

        // Test mode - her test API key'ini farklı merchant'a eşle
        if (apiKey.startsWith("pk_test_") || apiKey.equals("pk_merch001_live_abc123")) {
            switch (apiKey) {
                case "pk_test_merchant1":
                    return "TEST_MERCHANT";
                case "pk_test_merchant2":
                    return "TEST_MERCHANT_2";
                case "pk_test_merchant3":
                    return "TEST_MERCHANT_3";
                case "pk_merch001_live_abc123":
                    return "MERCH001"; // Bu API key için MERCH001 döndür
                default:
                    return "TEST_MERCHANT"; // Default test merchant
            }
        }

        // Production'da merchant'ı API key ile bulup merchant ID'yi döneriz
        return merchantAuthService.getMerchantByApiKey(apiKey)
                .map(merchant -> merchant.getMerchantId())
                .orElse(null);
    }
}