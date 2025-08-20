package com.payment.gateway.service;

import com.payment.gateway.dto.MandateRequest;
import com.payment.gateway.dto.MandateResponse;
import com.payment.gateway.model.Mandate;
import com.payment.gateway.repository.MandateRepository;
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
public class MandateService {
    
    private final MandateRepository mandateRepository;
    private final AuditService auditService;
    
    public MandateResponse createMandate(MandateRequest request) {
        try {
            // Generate unique mandate ID
            String mandateId = generateMandateId();
            
            // Create mandate entity
            Mandate mandate = new Mandate();
            mandate.setMandateId(mandateId);
            mandate.setCustomerId(request.getCustomerId());
            mandate.setMerchantId(request.getMerchantId());
            mandate.setAmount(request.getAmount());
            mandate.setCurrency(request.getCurrency());
            mandate.setStatus(Mandate.MandateStatus.PENDING);
            mandate.setType(request.getType());
            mandate.setBankAccountNumber(maskBankAccountNumber(request.getBankAccountNumber()));
            mandate.setBankSortCode(request.getBankSortCode());
            mandate.setAccountHolderName(request.getAccountHolderName());
            mandate.setDescription(request.getDescription());
            mandate.setStartDate(request.getStartDate());
            mandate.setEndDate(request.getEndDate());
            mandate.setFrequency(request.getFrequency());
            mandate.setMaxPayments(request.getMaxPayments());
            
            // Process mandate through gateway (simulated)
            Mandate.MandateStatus finalStatus = processMandateThroughGateway(mandate);
            mandate.setStatus(finalStatus);
            
            // Save mandate
            Mandate savedMandate = mandateRepository.save(mandate);
            
            // Audit logging
            auditService.createEvent()
                .eventType("MANDATE_CREATED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("CREATE")
                .resourceType("MANDATE")
                .resourceId(mandateId)
                .newValues(savedMandate)
                .additionalData("customerId", request.getCustomerId())
                .additionalData("merchantId", request.getMerchantId())
                .complianceTag("PCI_DSS")
                .log();
            
            log.info("Mandate created successfully with ID: {}", mandateId);
            
            return createMandateResponse(savedMandate, "Mandate created successfully", true);
            
        } catch (Exception e) {
            log.error("Error creating mandate: {}", e.getMessage());
            return createErrorResponse("Failed to create mandate: " + e.getMessage());
        }
    }
    
    public MandateResponse getMandateById(Long id) {
        Optional<Mandate> mandate = mandateRepository.findById(id);
        if (mandate.isPresent()) {
            return createMandateResponse(mandate.get(), "Mandate retrieved successfully", true);
        } else {
            return createErrorResponse("Mandate not found with ID: " + id);
        }
    }
    
    public MandateResponse getMandateByMandateId(String mandateId) {
        Optional<Mandate> mandate = mandateRepository.findByMandateId(mandateId);
        if (mandate.isPresent()) {
            return createMandateResponse(mandate.get(), "Mandate retrieved successfully", true);
        } else {
            return createErrorResponse("Mandate not found with mandate ID: " + mandateId);
        }
    }
    
    public List<MandateResponse> getAllMandates() {
        List<Mandate> mandates = mandateRepository.findAll();
        return mandates.stream()
                .map(mandate -> createMandateResponse(mandate, null, true))
                .collect(Collectors.toList());
    }
    
    public List<MandateResponse> getMandatesByCustomerId(String customerId) {
        List<Mandate> mandates = mandateRepository.findByCustomerId(customerId);
        return mandates.stream()
                .map(mandate -> createMandateResponse(mandate, null, true))
                .collect(Collectors.toList());
    }
    
    public List<MandateResponse> getMandatesByMerchantId(String merchantId) {
        List<Mandate> mandates = mandateRepository.findByMerchantId(merchantId);
        return mandates.stream()
                .map(mandate -> createMandateResponse(mandate, null, true))
                .collect(Collectors.toList());
    }
    
    public List<MandateResponse> getMandatesByStatus(Mandate.MandateStatus status) {
        List<Mandate> mandates = mandateRepository.findByStatus(status);
        return mandates.stream()
                .map(mandate -> createMandateResponse(mandate, null, true))
                .collect(Collectors.toList());
    }
    
    public List<MandateResponse> getActiveCustomerMandates(String customerId) {
        List<Mandate> mandates = mandateRepository.findActiveCustomerMandatesOnDate(customerId, LocalDateTime.now());
        return mandates.stream()
                .map(mandate -> createMandateResponse(mandate, null, true))
                .collect(Collectors.toList());
    }
    
    public MandateResponse revokeMandate(Long id) {
        Optional<Mandate> mandateOpt = mandateRepository.findById(id);
        if (mandateOpt.isPresent()) {
            Mandate mandate = mandateOpt.get();
            
            if (mandate.getStatus() == Mandate.MandateStatus.ACTIVE || 
                mandate.getStatus() == Mandate.MandateStatus.PENDING) {
                
                mandate.setStatus(Mandate.MandateStatus.REVOKED);
                mandate.setGatewayResponse("Mandate revoked");
                Mandate updatedMandate = mandateRepository.save(mandate);
                
                log.info("Mandate revoked successfully with ID: {}", id);
                return createMandateResponse(updatedMandate, "Mandate revoked successfully", true);
            } else {
                return createErrorResponse("Cannot revoke mandate with status: " + mandate.getStatus());
            }
        } else {
            return createErrorResponse("Mandate not found with ID: " + id);
        }
    }
    
    public MandateResponse updateMandateStatus(Long id, Mandate.MandateStatus newStatus) {
        Optional<Mandate> mandateOpt = mandateRepository.findById(id);
        if (mandateOpt.isPresent()) {
            Mandate mandate = mandateOpt.get();
            mandate.setStatus(newStatus);
            mandate.setGatewayResponse("Status updated to: " + newStatus);
            Mandate updatedMandate = mandateRepository.save(mandate);
            
            log.info("Mandate status updated to {} for ID: {}", newStatus, id);
            return createMandateResponse(updatedMandate, "Mandate status updated successfully", true);
        } else {
            return createErrorResponse("Mandate not found with ID: " + id);
        }
    }
    
    private String generateMandateId() {
        return "MND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String maskBankAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }
    
    private Mandate.MandateStatus processMandateThroughGateway(Mandate mandate) {
        // Simulate mandate gateway processing
        // In real implementation, this would call external mandate gateway API
        
        try {
            // Simulate processing time
            Thread.sleep(100);
            
            // Simulate success/failure based on account number
            if (mandate.getBankAccountNumber().endsWith("0000")) {
                mandate.setGatewayResponse("Mandate failed: Invalid account");
                mandate.setGatewayMandateId("GMND-" + UUID.randomUUID().toString().substring(0, 8));
                return Mandate.MandateStatus.CANCELLED;
            } else {
                mandate.setGatewayResponse("Mandate processed successfully");
                mandate.setGatewayMandateId("GMND-" + UUID.randomUUID().toString().substring(0, 8));
                return Mandate.MandateStatus.ACTIVE;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            mandate.setGatewayResponse("Mandate processing interrupted");
            return Mandate.MandateStatus.CANCELLED;
        }
    }
    
    private MandateResponse createMandateResponse(Mandate mandate, String message, boolean success) {
        MandateResponse response = new MandateResponse();
        response.setId(mandate.getId());
        response.setMandateId(mandate.getMandateId());
        response.setCustomerId(mandate.getCustomerId());
        response.setMerchantId(mandate.getMerchantId());
        response.setAmount(mandate.getAmount());
        response.setCurrency(mandate.getCurrency());
        response.setStatus(mandate.getStatus());
        response.setType(mandate.getType());
        response.setBankAccountNumber(mandate.getBankAccountNumber());
        response.setBankSortCode(mandate.getBankSortCode());
        response.setAccountHolderName(mandate.getAccountHolderName());
        response.setDescription(mandate.getDescription());
        response.setStartDate(mandate.getStartDate());
        response.setEndDate(mandate.getEndDate());
        response.setFrequency(mandate.getFrequency());
        response.setMaxPayments(mandate.getMaxPayments());
        response.setGatewayResponse(mandate.getGatewayResponse());
        response.setGatewayMandateId(mandate.getGatewayMandateId());
        response.setCreatedAt(mandate.getCreatedAt());
        response.setUpdatedAt(mandate.getUpdatedAt());
        response.setMessage(message);
        response.setSuccess(success);
        return response;
    }
    
    private MandateResponse createErrorResponse(String errorMessage) {
        MandateResponse response = new MandateResponse();
        response.setMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
}
