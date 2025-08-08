package com.payment.gateway.service;

import com.payment.gateway.dto.RefundRequest;
import com.payment.gateway.dto.RefundResponse;
import com.payment.gateway.model.Refund;
import com.payment.gateway.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundService {
    
    private final RefundRepository refundRepository;
    
    public RefundResponse createRefund(RefundRequest request) {
        try {
            // Generate unique refund ID
            String refundId = generateRefundId();
            
            // Create refund entity
            Refund refund = new Refund();
            refund.setRefundId(refundId);
            refund.setPaymentId(request.getPaymentId());
            refund.setTransactionId(request.getTransactionId());
            refund.setMerchantId(request.getMerchantId());
            refund.setCustomerId(request.getCustomerId());
            refund.setAmount(request.getAmount());
            refund.setCurrency(request.getCurrency());
            refund.setStatus(Refund.RefundStatus.PENDING);
            refund.setReason(request.getReason());
            refund.setDescription(request.getDescription());
            refund.setRefundDate(LocalDateTime.now());
            
            // Process refund through gateway (simulated)
            Refund.RefundStatus finalStatus = processRefundThroughGateway(refund);
            refund.setStatus(finalStatus);
            
            // Save refund
            Refund savedRefund = refundRepository.save(refund);
            
            log.info("Refund created successfully with ID: {}", refundId);
            
            return createRefundResponse(savedRefund, "Refund created successfully", true);
            
        } catch (Exception e) {
            log.error("Error creating refund: {}", e.getMessage());
            return createErrorResponse("Failed to create refund: " + e.getMessage());
        }
    }
    
    public RefundResponse getRefundById(Long id) {
        Optional<Refund> refund = refundRepository.findById(id);
        if (refund.isPresent()) {
            return createRefundResponse(refund.get(), "Refund retrieved successfully", true);
        } else {
            return createErrorResponse("Refund not found with ID: " + id);
        }
    }
    
    public RefundResponse getRefundByRefundId(String refundId) {
        Optional<Refund> refund = refundRepository.findByRefundId(refundId);
        if (refund.isPresent()) {
            return createRefundResponse(refund.get(), "Refund retrieved successfully", true);
        } else {
            return createErrorResponse("Refund not found with refund ID: " + refundId);
        }
    }
    
    public RefundResponse getRefundByPaymentId(String paymentId) {
        Optional<Refund> refund = refundRepository.findByPaymentId(paymentId);
        if (refund.isPresent()) {
            return createRefundResponse(refund.get(), "Refund retrieved successfully", true);
        } else {
            return createErrorResponse("Refund not found with payment ID: " + paymentId);
        }
    }
    
    public List<RefundResponse> getAllRefunds() {
        List<Refund> refunds = refundRepository.findAll();
        return refunds.stream()
                .map(refund -> createRefundResponse(refund, null, true))
                .collect(Collectors.toList());
    }
    
    public List<RefundResponse> getRefundsByMerchantId(String merchantId) {
        List<Refund> refunds = refundRepository.findByMerchantId(merchantId);
        return refunds.stream()
                .map(refund -> createRefundResponse(refund, null, true))
                .collect(Collectors.toList());
    }
    
    public List<RefundResponse> getRefundsByCustomerId(String customerId) {
        List<Refund> refunds = refundRepository.findByCustomerId(customerId);
        return refunds.stream()
                .map(refund -> createRefundResponse(refund, null, true))
                .collect(Collectors.toList());
    }
    
    public List<RefundResponse> getRefundsByStatus(Refund.RefundStatus status) {
        List<Refund> refunds = refundRepository.findByStatus(status);
        return refunds.stream()
                .map(refund -> createRefundResponse(refund, null, true))
                .collect(Collectors.toList());
    }
    
    public List<RefundResponse> getRefundsByReason(Refund.RefundReason reason) {
        List<Refund> refunds = refundRepository.findByReason(reason);
        return refunds.stream()
                .map(refund -> createRefundResponse(refund, null, true))
                .collect(Collectors.toList());
    }
    
    public List<RefundResponse> getRefundsByTransactionId(String transactionId) {
        List<Refund> refunds = refundRepository.findByTransactionId(transactionId);
        return refunds.stream()
                .map(refund -> createRefundResponse(refund, null, true))
                .collect(Collectors.toList());
    }
    
    public RefundResponse updateRefund(Long id, RefundRequest request) {
        Optional<Refund> refundOpt = refundRepository.findById(id);
        if (refundOpt.isPresent()) {
            Refund refund = refundOpt.get();
            
            // Update refund fields
            refund.setAmount(request.getAmount());
            refund.setCurrency(request.getCurrency());
            refund.setReason(request.getReason());
            refund.setDescription(request.getDescription());
            
            Refund updatedRefund = refundRepository.save(refund);
            
            log.info("Refund updated successfully with ID: {}", id);
            return createRefundResponse(updatedRefund, "Refund updated successfully", true);
        } else {
            return createErrorResponse("Refund not found with ID: " + id);
        }
    }
    
    public RefundResponse updateRefundStatus(Long id, Refund.RefundStatus newStatus) {
        Optional<Refund> refundOpt = refundRepository.findById(id);
        if (refundOpt.isPresent()) {
            Refund refund = refundOpt.get();
            refund.setStatus(newStatus);
            refund.setGatewayResponse("Status updated to: " + newStatus);
            Refund updatedRefund = refundRepository.save(refund);
            
            log.info("Refund status updated to {} for ID: {}", newStatus, id);
            return createRefundResponse(updatedRefund, "Refund status updated successfully", true);
        } else {
            return createErrorResponse("Refund not found with ID: " + id);
        }
    }
    
    private String generateRefundId() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private Refund.RefundStatus processRefundThroughGateway(Refund refund) {
        // Simulate refund gateway processing
        // In real implementation, this would call external refund gateway API
        
        try {
            // Simulate processing time
            Thread.sleep(100);
            
            // Simulate success/failure based on amount
            if (refund.getAmount().compareTo(java.math.BigDecimal.valueOf(1000)) > 0) {
                refund.setGatewayResponse("Refund failed: Amount too high");
                refund.setGatewayRefundId("GREF-" + UUID.randomUUID().toString().substring(0, 8));
                return Refund.RefundStatus.FAILED;
            } else {
                refund.setGatewayResponse("Refund processed successfully");
                refund.setGatewayRefundId("GREF-" + UUID.randomUUID().toString().substring(0, 8));
                return Refund.RefundStatus.COMPLETED;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            refund.setGatewayResponse("Refund processing interrupted");
            return Refund.RefundStatus.FAILED;
        }
    }
    
    private RefundResponse createRefundResponse(Refund refund, String message, boolean success) {
        RefundResponse response = new RefundResponse();
        response.setId(refund.getId());
        response.setRefundId(refund.getRefundId());
        response.setPaymentId(refund.getPaymentId());
        response.setTransactionId(refund.getTransactionId());
        response.setMerchantId(refund.getMerchantId());
        response.setCustomerId(refund.getCustomerId());
        response.setAmount(refund.getAmount());
        response.setCurrency(refund.getCurrency());
        response.setStatus(refund.getStatus());
        response.setReason(refund.getReason());
        response.setDescription(refund.getDescription());
        response.setGatewayResponse(refund.getGatewayResponse());
        response.setGatewayRefundId(refund.getGatewayRefundId());
        response.setRefundDate(refund.getRefundDate());
        response.setCreatedAt(refund.getCreatedAt());
        response.setUpdatedAt(refund.getUpdatedAt());
        response.setMessage(message);
        response.setSuccess(success);
        return response;
    }
    
    private RefundResponse createErrorResponse(String errorMessage) {
        RefundResponse response = new RefundResponse();
        response.setMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
}
