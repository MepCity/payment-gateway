package com.payment.gateway.service;

import com.payment.gateway.dto.PayoutRequest;
import com.payment.gateway.dto.PayoutResponse;
import com.payment.gateway.model.Payout;
import com.payment.gateway.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.payment.gateway.service.AuditService;
import com.payment.gateway.model.AuditLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayoutService {
    
    private final PayoutRepository payoutRepository;
    private final AuditService auditService;
    
    public PayoutResponse createPayout(PayoutRequest request) {
        try {
            log.info("Creating payout for merchant: {}, customer: {}, amount: {}", 
                    request.getMerchantId(), request.getCustomerId(), request.getAmount());
            
            // Generate unique payout ID
            String payoutId = generatePayoutId();
            
            // Create payout entity
            Payout payout = new Payout();
            payout.setPayoutId(payoutId);
            payout.setMerchantId(request.getMerchantId());
            payout.setCustomerId(request.getCustomerId());
            payout.setAmount(request.getAmount());
            payout.setCurrency(request.getCurrency());
            payout.setStatus(Payout.PayoutStatus.PENDING);
            payout.setType(request.getType());
            payout.setBankAccountNumber(request.getBankAccountNumber());
            payout.setBankRoutingNumber(request.getBankRoutingNumber());
            payout.setBankName(request.getBankName());
            payout.setAccountHolderName(request.getAccountHolderName());
            payout.setDescription(request.getDescription());
            payout.setNotes(request.getNotes());
            
            // Process through gateway
            processPayoutThroughGateway(payout);
            
            // Save to database
            Payout savedPayout = payoutRepository.save(payout);
            
            // Audit logging
            auditService.createEvent()
                .eventType("PAYOUT_CREATED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("CREATE")
                .resourceType("PAYOUT")
                .resourceId(payoutId)
                .newValues(savedPayout)
                .additionalData("merchantId", request.getMerchantId())
                .additionalData("customerId", request.getCustomerId())
                .additionalData("amount", request.getAmount().toString())
                .complianceTag("PCI_DSS")
                .log();
            
            log.info("Payout created successfully with ID: {}", payoutId);
            return createPayoutResponse(savedPayout, true, "Payout created successfully");
            
        } catch (Exception e) {
            log.error("Error creating payout: {}", e.getMessage(), e);
            return createErrorResponse("Failed to create payout: " + e.getMessage());
        }
    }
    
    public PayoutResponse getPayoutById(Long id) {
        try {
            Optional<Payout> payout = payoutRepository.findById(id);
            if (payout.isPresent()) {
                return createPayoutResponse(payout.get(), true, "Payout retrieved successfully");
            } else {
                return createErrorResponse("Payout not found with ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error retrieving payout by ID {}: {}", id, e.getMessage(), e);
            return createErrorResponse("Failed to retrieve payout: " + e.getMessage());
        }
    }
    
    public PayoutResponse getPayoutByPayoutId(String payoutId) {
        try {
            Optional<Payout> payout = payoutRepository.findByPayoutId(payoutId);
            if (payout.isPresent()) {
                return createPayoutResponse(payout.get(), true, "Payout retrieved successfully");
            } else {
                return createErrorResponse("Payout not found with payout ID: " + payoutId);
            }
        } catch (Exception e) {
            log.error("Error retrieving payout by payout ID {}: {}", payoutId, e.getMessage(), e);
            return createErrorResponse("Failed to retrieve payout: " + e.getMessage());
        }
    }
    
    public List<PayoutResponse> getAllPayouts() {
        try {
            List<Payout> payouts = payoutRepository.findAll();
            return payouts.stream()
                    .map(payout -> createPayoutResponse(payout, true, "Payout retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving all payouts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payouts", e);
        }
    }
    
    public List<PayoutResponse> getPayoutsByMerchantId(String merchantId) {
        try {
            List<Payout> payouts = payoutRepository.findByMerchantId(merchantId);
            return payouts.stream()
                    .map(payout -> createPayoutResponse(payout, true, "Payout retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving payouts for merchant {}: {}", merchantId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payouts for merchant", e);
        }
    }
    
    public List<PayoutResponse> getPayoutsByCustomerId(String customerId) {
        try {
            List<Payout> payouts = payoutRepository.findByCustomerId(customerId);
            return payouts.stream()
                    .map(payout -> createPayoutResponse(payout, true, "Payout retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving payouts for customer {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payouts for customer", e);
        }
    }
    
    public List<PayoutResponse> getPayoutsByStatus(Payout.PayoutStatus status) {
        try {
            List<Payout> payouts = payoutRepository.findByStatus(status);
            return payouts.stream()
                    .map(payout -> createPayoutResponse(payout, true, "Payout retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving payouts by status {}: {}", status, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payouts by status", e);
        }
    }
    
    public List<PayoutResponse> getPayoutsByType(Payout.PayoutType type) {
        try {
            List<Payout> payouts = payoutRepository.findByType(type);
            return payouts.stream()
                    .map(payout -> createPayoutResponse(payout, true, "Payout retrieved successfully"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving payouts by type {}: {}", type, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payouts by type", e);
        }
    }
    
    public PayoutResponse updatePayoutStatus(Long id, Payout.PayoutStatus status) {
        try {
            Optional<Payout> payoutOpt = payoutRepository.findById(id);
            if (payoutOpt.isPresent()) {
                Payout payout = payoutOpt.get();
                payout.setStatus(status);
                
                if (status == Payout.PayoutStatus.COMPLETED) {
                    payout.setSettledAt(LocalDateTime.now());
                } else if (status == Payout.PayoutStatus.PROCESSING) {
                    payout.setProcessedAt(LocalDateTime.now());
                }
                
                Payout updatedPayout = payoutRepository.save(payout);
                log.info("Payout status updated to {} for ID: {}", status, id);
                return createPayoutResponse(updatedPayout, true, "Payout status updated successfully");
            } else {
                return createErrorResponse("Payout not found with ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error updating payout status for ID {}: {}", id, e.getMessage(), e);
            return createErrorResponse("Failed to update payout status: " + e.getMessage());
        }
    }
    
    public PayoutResponse cancelPayout(Long id) {
        try {
            Optional<Payout> payoutOpt = payoutRepository.findById(id);
            if (payoutOpt.isPresent()) {
                Payout payout = payoutOpt.get();
                
                if (payout.getStatus() == Payout.PayoutStatus.PENDING) {
                    payout.setStatus(Payout.PayoutStatus.CANCELLED);
                    payout.setNotes(payout.getNotes() + " - Cancelled by user");
                    Payout updatedPayout = payoutRepository.save(payout);
                    log.info("Payout cancelled for ID: {}", id);
                    return createPayoutResponse(updatedPayout, true, "Payout cancelled successfully");
                } else {
                    return createErrorResponse("Cannot cancel payout with status: " + payout.getStatus());
                }
            } else {
                return createErrorResponse("Payout not found with ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error cancelling payout for ID {}: {}", id, e.getMessage(), e);
            return createErrorResponse("Failed to cancel payout: " + e.getMessage());
        }
    }
    
    public PayoutResponse deletePayout(Long id) {
        try {
            if (payoutRepository.existsById(id)) {
                payoutRepository.deleteById(id);
                log.info("Payout deleted with ID: {}", id);
                
                PayoutResponse response = new PayoutResponse();
                response.setSuccess(true);
                response.setMessage("Payout deleted successfully");
                return response;
            } else {
                PayoutResponse response = new PayoutResponse();
                response.setSuccess(false);
                response.setMessage("Payout not found with ID: " + id);
                return response;
            }
        } catch (Exception e) {
            log.error("Error deleting payout with ID {}: {}", id, e.getMessage(), e);
            PayoutResponse response = new PayoutResponse();
            response.setSuccess(false);
            response.setMessage("Failed to delete payout: " + e.getMessage());
            return response;
        }
    }
    
    public BigDecimal getTotalPayoutAmountByMerchant(String merchantId) {
        try {
            BigDecimal total = payoutRepository.sumAmountByStatusAndMerchantId(Payout.PayoutStatus.COMPLETED, merchantId);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating total payout amount for merchant {}: {}", merchantId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    public long getPayoutCountByStatus(Payout.PayoutStatus status) {
        try {
            return payoutRepository.countByStatus(status);
        } catch (Exception e) {
            log.error("Error counting payouts by status {}: {}", status, e.getMessage(), e);
            return 0;
        }
    }
    
    // Helper methods
    private String generatePayoutId() {
        return "POUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String maskBankAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
    
    private void processPayoutThroughGateway(Payout payout) {
        // Simulate gateway processing
        log.info("Processing payout through gateway: {}", payout.getPayoutId());
        
        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate gateway response
        payout.setGatewayPayoutId("GW-POUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payout.setGatewayResponse("{\"status\":\"processing\",\"message\":\"Payout submitted successfully\"}");
        
        // Simulate status update based on type
        if (payout.getType() == Payout.PayoutType.WIRE_TRANSFER || payout.getType() == Payout.PayoutType.SWIFT_TRANSFER) {
            payout.setStatus(Payout.PayoutStatus.PROCESSING);
            payout.setProcessedAt(LocalDateTime.now());
        } else {
            payout.setStatus(Payout.PayoutStatus.PENDING);
        }
    }
    
    private PayoutResponse createPayoutResponse(Payout payout, boolean success, String message) {
        PayoutResponse response = new PayoutResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setPayoutId(payout.getPayoutId());
        response.setMerchantId(payout.getMerchantId());
        response.setCustomerId(payout.getCustomerId());
        response.setAmount(payout.getAmount());
        response.setCurrency(payout.getCurrency());
        response.setStatus(payout.getStatus());
        response.setType(payout.getType());
        response.setMaskedBankAccountNumber(maskBankAccountNumber(payout.getBankAccountNumber()));
        response.setBankName(payout.getBankName());
        response.setAccountHolderName(payout.getAccountHolderName());
        response.setDescription(payout.getDescription());
        response.setGatewayPayoutId(payout.getGatewayPayoutId());
        response.setProcessedAt(payout.getProcessedAt());
        response.setSettledAt(payout.getSettledAt());
        response.setFailureReason(payout.getFailureReason());
        response.setNotes(payout.getNotes());
        response.setCreatedAt(payout.getCreatedAt());
        response.setUpdatedAt(payout.getUpdatedAt());
        return response;
    }
    
    private PayoutResponse createErrorResponse(String message) {
        PayoutResponse response = new PayoutResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}