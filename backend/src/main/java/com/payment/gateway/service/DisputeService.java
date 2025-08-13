package com.payment.gateway.service;

import com.payment.gateway.dto.DisputeRequest;
import com.payment.gateway.dto.DisputeResponse;
import com.payment.gateway.model.Dispute;
import com.payment.gateway.repository.DisputeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.payment.gateway.service.AuditService;
import com.payment.gateway.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DisputeService {
    
    private final DisputeRepository disputeRepository;
    private final AuditService auditService;
    
    public DisputeResponse createDispute(DisputeRequest request) {
        try {
            // Generate unique dispute ID
            String disputeId = generateDisputeId();
            
            // Create dispute entity
            Dispute dispute = new Dispute();
            dispute.setDisputeId(disputeId);
            dispute.setPaymentId(request.getPaymentId());
            dispute.setTransactionId(request.getTransactionId());
            dispute.setMerchantId(request.getMerchantId());
            dispute.setCustomerId(request.getCustomerId());
            dispute.setAmount(request.getAmount());
            dispute.setCurrency(request.getCurrency());
            dispute.setStatus(Dispute.DisputeStatus.OPENED);
            dispute.setReason(request.getReason());
            dispute.setDescription(request.getDescription());
            dispute.setEvidence(request.getEvidence());
            dispute.setDisputeDate(LocalDateTime.now());
            
            // Process dispute through gateway (simulated)
            Dispute.DisputeStatus finalStatus = processDisputeThroughGateway(dispute);
            dispute.setStatus(finalStatus);
            
            // Save dispute
            Dispute savedDispute = disputeRepository.save(dispute);
            
            // Audit logging
            auditService.createEvent()
                .eventType("DISPUTE_CREATED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("CREATE")
                .resourceType("DISPUTE")
                .resourceId(disputeId)
                .newValues(savedDispute)
                .additionalData("paymentId", request.getPaymentId())
                .additionalData("merchantId", request.getMerchantId())
                .complianceTag("PCI_DSS")
                .log();
            
            log.info("Dispute created successfully with ID: {}", disputeId);
            
            return createDisputeResponse(savedDispute, "Dispute created successfully", true);
            
        } catch (Exception e) {
            log.error("Error creating dispute: {}", e.getMessage());
            return createErrorResponse("Failed to create dispute: " + e.getMessage());
        }
    }
    
    public DisputeResponse getDisputeById(Long id) {
        Optional<Dispute> dispute = disputeRepository.findById(id);
        if (dispute.isPresent()) {
            return createDisputeResponse(dispute.get(), "Dispute retrieved successfully", true);
        } else {
            return createErrorResponse("Dispute not found with ID: " + id);
        }
    }
    
    public DisputeResponse getDisputeByDisputeId(String disputeId) {
        Optional<Dispute> dispute = disputeRepository.findByDisputeId(disputeId);
        if (dispute.isPresent()) {
            return createDisputeResponse(dispute.get(), "Dispute retrieved successfully", true);
        } else {
            return createErrorResponse("Dispute not found with dispute ID: " + disputeId);
        }
    }
    
    public DisputeResponse getDisputeByPaymentId(String paymentId) {
        Optional<Dispute> dispute = disputeRepository.findByPaymentId(paymentId);
        if (dispute.isPresent()) {
            return createDisputeResponse(dispute.get(), "Dispute retrieved successfully", true);
        } else {
            return createErrorResponse("Dispute not found with payment ID: " + paymentId);
        }
    }
    
    public List<DisputeResponse> getAllDisputes() {
        List<Dispute> disputes = disputeRepository.findAll();
        return disputes.stream()
                .map(dispute -> createDisputeResponse(dispute, null, true))
                .collect(Collectors.toList());
    }
    
    public List<DisputeResponse> getDisputesByMerchantId(String merchantId) {
        List<Dispute> disputes = disputeRepository.findByMerchantId(merchantId);
        return disputes.stream()
                .map(dispute -> createDisputeResponse(dispute, null, true))
                .collect(Collectors.toList());
    }
    
    public List<DisputeResponse> getDisputesByCustomerId(String customerId) {
        List<Dispute> disputes = disputeRepository.findByCustomerId(customerId);
        return disputes.stream()
                .map(dispute -> createDisputeResponse(dispute, null, true))
                .collect(Collectors.toList());
    }
    
    public List<DisputeResponse> getDisputesByStatus(Dispute.DisputeStatus status) {
        List<Dispute> disputes = disputeRepository.findByStatus(status);
        return disputes.stream()
                .map(dispute -> createDisputeResponse(dispute, null, true))
                .collect(Collectors.toList());
    }
    
    public List<DisputeResponse> getDisputesByReason(Dispute.DisputeReason reason) {
        List<Dispute> disputes = disputeRepository.findByReason(reason);
        return disputes.stream()
                .map(dispute -> createDisputeResponse(dispute, null, true))
                .collect(Collectors.toList());
    }
    
    public List<DisputeResponse> getDisputesByTransactionId(String transactionId) {
        List<Dispute> disputes = disputeRepository.findByTransactionId(transactionId);
        return disputes.stream()
                .map(dispute -> createDisputeResponse(dispute, null, true))
                .collect(Collectors.toList());
    }
    
    public DisputeResponse updateDispute(Long id, DisputeRequest request) {
        Optional<Dispute> disputeOpt = disputeRepository.findById(id);
        if (disputeOpt.isPresent()) {
            Dispute dispute = disputeOpt.get();
            
            // Update dispute fields
            dispute.setAmount(request.getAmount());
            dispute.setCurrency(request.getCurrency());
            dispute.setReason(request.getReason());
            dispute.setDescription(request.getDescription());
            dispute.setEvidence(request.getEvidence());
            
            Dispute updatedDispute = disputeRepository.save(dispute);
            
            log.info("Dispute updated successfully with ID: {}", id);
            return createDisputeResponse(updatedDispute, "Dispute updated successfully", true);
        } else {
            return createErrorResponse("Dispute not found with ID: " + id);
        }
    }
    
    public DisputeResponse updateDisputeStatus(Long id, Dispute.DisputeStatus newStatus) {
        Optional<Dispute> disputeOpt = disputeRepository.findById(id);
        if (disputeOpt.isPresent()) {
            Dispute dispute = disputeOpt.get();
            dispute.setStatus(newStatus);
            
            // Set resolution date if dispute is resolved
            if (newStatus == Dispute.DisputeStatus.RESOLVED || 
                newStatus == Dispute.DisputeStatus.WON || 
                newStatus == Dispute.DisputeStatus.LOST) {
                dispute.setResolutionDate(LocalDateTime.now());
            }
            
            dispute.setGatewayResponse("Status updated to: " + newStatus);
            Dispute updatedDispute = disputeRepository.save(dispute);
            
            log.info("Dispute status updated to {} for ID: {}", newStatus, id);
            return createDisputeResponse(updatedDispute, "Dispute status updated successfully", true);
        } else {
            return createErrorResponse("Dispute not found with ID: " + id);
        }
    }
    
    public DisputeResponse closeDispute(Long id) {
        Optional<Dispute> disputeOpt = disputeRepository.findById(id);
        if (disputeOpt.isPresent()) {
            Dispute dispute = disputeOpt.get();
            
            if (dispute.getStatus() == Dispute.DisputeStatus.OPENED || 
                dispute.getStatus() == Dispute.DisputeStatus.UNDER_REVIEW) {
                
                dispute.setStatus(Dispute.DisputeStatus.CLOSED);
                dispute.setResolutionDate(LocalDateTime.now());
                dispute.setGatewayResponse("Dispute closed");
                Dispute updatedDispute = disputeRepository.save(dispute);
                
                log.info("Dispute closed successfully with ID: {}", id);
                return createDisputeResponse(updatedDispute, "Dispute closed successfully", true);
            } else {
                return createErrorResponse("Cannot close dispute with status: " + dispute.getStatus());
            }
        } else {
            return createErrorResponse("Dispute not found with ID: " + id);
        }
    }
    
    private String generateDisputeId() {
        return "DSP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private Dispute.DisputeStatus processDisputeThroughGateway(Dispute dispute) {
        // Simulate dispute gateway processing
        // In real implementation, this would call external dispute gateway API
        
        try {
            // Simulate processing time
            Thread.sleep(100);
            
            // Simulate success/failure based on reason
            if (dispute.getReason() == Dispute.DisputeReason.FRAUD) {
                dispute.setGatewayResponse("Dispute under review - fraud investigation required");
                dispute.setGatewayDisputeId("GDSP-" + UUID.randomUUID().toString().substring(0, 8));
                return Dispute.DisputeStatus.UNDER_REVIEW;
            } else {
                dispute.setGatewayResponse("Dispute opened successfully");
                dispute.setGatewayDisputeId("GDSP-" + UUID.randomUUID().toString().substring(0, 8));
                return Dispute.DisputeStatus.OPENED;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            dispute.setGatewayResponse("Dispute processing interrupted");
            return Dispute.DisputeStatus.OPENED;
        }
    }
    
    private DisputeResponse createDisputeResponse(Dispute dispute, String message, boolean success) {
        DisputeResponse response = new DisputeResponse();
        response.setId(dispute.getId());
        response.setDisputeId(dispute.getDisputeId());
        response.setPaymentId(dispute.getPaymentId());
        response.setTransactionId(dispute.getTransactionId());
        response.setMerchantId(dispute.getMerchantId());
        response.setCustomerId(dispute.getCustomerId());
        response.setAmount(dispute.getAmount());
        response.setCurrency(dispute.getCurrency());
        response.setStatus(dispute.getStatus());
        response.setReason(dispute.getReason());
        response.setDescription(dispute.getDescription());
        response.setEvidence(dispute.getEvidence());
        response.setGatewayResponse(dispute.getGatewayResponse());
        response.setGatewayDisputeId(dispute.getGatewayDisputeId());
        response.setDisputeDate(dispute.getDisputeDate());
        response.setResolutionDate(dispute.getResolutionDate());
        response.setCreatedAt(dispute.getCreatedAt());
        response.setUpdatedAt(dispute.getUpdatedAt());
        response.setMessage(message);
        response.setSuccess(success);
        return response;
    }
    
    private DisputeResponse createErrorResponse(String errorMessage) {
        DisputeResponse response = new DisputeResponse();
        response.setMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
}
