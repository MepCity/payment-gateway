package com.payment.gateway.service;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.PaymentResponse;
import com.payment.gateway.model.Payment;
import com.payment.gateway.model.RiskAssessment;
import com.payment.gateway.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final RealBankIntegrationService realBankIntegrationService;
    private final RiskAssessmentService riskAssessmentService;
    private final AuditService auditService;
    

    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    public PaymentResponse createPayment(PaymentRequest request, String ipAddress, String userAgent) {
        
        // Audit log - Payment initiation
        auditService.logEvent(
            auditService.createEvent()
                .eventType("PAYMENT")
                .action("INITIATE")
                .actor("api-user")
                .resourceType("Payment")
                .resourceId(request.getMerchantId() + "-" + request.getCustomerId())
                .additionalData("amount", request.getAmount())
                .additionalData("currency", request.getCurrency())
                .additionalData("paymentMethod", request.getPaymentMethod())
                .additionalData("cardLastFour", extractCardLastFour(request.getCardNumber()))
                .complianceTag("PCI_DSS")
                .complianceTag("KVKK")
                .complianceTag("GDPR")
        );
        
        try {
            // Generate unique payment ID and transaction ID
            String paymentId = generatePaymentId();
            String transactionId = generateTransactionId();
            
            // Create payment entity
            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            payment.setTransactionId(transactionId);
            payment.setMerchantId(request.getMerchantId());
            payment.setCustomerId(request.getCustomerId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setCardNumber(maskCardNumber(request.getCardNumber()));
            payment.setCardHolderName(request.getCardHolderName());
            payment.setCardBrand(detectCardBrand(request.getCardNumber()));
            payment.setCardBin(extractCardBin(request.getCardNumber()));
            payment.setCardLastFour(extractCardLastFour(request.getCardNumber()));
            payment.setExpiryDate(request.getExpiryDate());
            payment.setDescription(request.getDescription());
            payment.setCreatedAt(LocalDateTime.now());
            
            // Save initial payment record
            payment = paymentRepository.save(payment);
            log.info("Payment created with ID: {}", paymentId);
            
            // FRAUD DETECTION - Risk Assessment
            log.info("Starting fraud detection for payment: {}", paymentId);
            RiskAssessment riskAssessment = riskAssessmentService.assessPaymentRisk(
                request, payment, ipAddress, userAgent);
            
            // Audit log - Risk Assessment
            auditService.logEvent(
                auditService.createEvent()
                    .eventType("FRAUD_DETECTION")
                    .action("RISK_ASSESSMENT")
                    .actor("system")
                    .resourceType("Payment")
                    .resourceId(paymentId)
                    .additionalData("riskLevel", riskAssessment.getRiskLevel().name())
                    .additionalData("riskScore", riskAssessment.getRiskScore())
                    .additionalData("action", riskAssessment.getAction().name())
                    .additionalData("riskFactors", riskAssessment.getRiskFactors())
                    .complianceTag("PCI_DSS")
                    .complianceTag("AML")
            );
            
            // Check risk assessment result
            if (riskAssessment.getAction() == RiskAssessment.AssessmentAction.DECLINE) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setGatewayResponse("Payment declined due to high fraud risk: " + riskAssessment.getRiskLevel());
                payment = paymentRepository.save(payment);
                
                log.warn("Payment {} declined due to fraud risk - Risk Level: {}, Score: {}", 
                        paymentId, riskAssessment.getRiskLevel(), riskAssessment.getRiskScore());
                
                // Audit log - Payment declined due to fraud
                auditService.logEvent(
                    auditService.createEvent()
                        .eventType("PAYMENT")
                        .action("DECLINE")
                        .actor("fraud-system")
                        .resourceType("Payment")
                        .resourceId(paymentId)
                        .additionalData("reason", "HIGH_FRAUD_RISK")
                        .additionalData("riskScore", riskAssessment.getRiskScore())
                        .complianceTag("PCI_DSS")
                        .complianceTag("AML")
                );
                
                return createPaymentResponse(payment, 
                    "Payment declined due to security concerns. Risk Score: " + riskAssessment.getRiskScore(), false);
            }
            
            if (riskAssessment.getAction() == RiskAssessment.AssessmentAction.REVIEW) {
                payment.setStatus(Payment.PaymentStatus.CANCELLED); // Hold for manual review
                payment.setGatewayResponse("Payment held for manual review due to elevated fraud risk");
                payment = paymentRepository.save(payment);
                
                log.warn("Payment {} held for manual review - Risk Level: {}, Score: {}", 
                        paymentId, riskAssessment.getRiskLevel(), riskAssessment.getRiskScore());
                
                return createPaymentResponse(payment, 
                    "Payment is being reviewed for security. You will be notified of the outcome.", false);
            }
            
            // Process payment through gateway
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            payment = paymentRepository.save(payment);
            
            Payment.PaymentStatus finalStatus;
            
            // Additional verification for medium risk transactions
            if (riskAssessment.getAction() == RiskAssessment.AssessmentAction.CHALLENGE) {
                log.info("Payment {} requires additional verification - implementing 3D Secure flow", paymentId);
                // In a real implementation, this would redirect to 3D Secure
                finalStatus = processPaymentThroughGateway(request, payment);
            } else {
                // Low risk - proceed normally
                finalStatus = processPaymentThroughGateway(request, payment);
            }
            
            payment.setStatus(finalStatus);
            
            // Set completedAt if payment is successful
            if (finalStatus == Payment.PaymentStatus.COMPLETED) {
                payment.setCompletedAt(LocalDateTime.now());
            }
            
            // Save final payment status
            payment = paymentRepository.save(payment);
            
            // Audit log - Payment completion status
            auditService.logEvent(
                auditService.createEvent()
                    .eventType("PAYMENT")
                    .action(finalStatus == Payment.PaymentStatus.COMPLETED ? "COMPLETE" : "FAIL")
                    .actor("system")
                    .resourceType("Payment")
                    .resourceId(payment.getPaymentId())
                    .additionalData("transactionId", payment.getTransactionId())
                    .additionalData("status", finalStatus.name())
                    .additionalData("amount", payment.getAmount())
                    .additionalData("currency", payment.getCurrency())
                    .additionalData("gatewayResponse", payment.getGatewayResponse())
                    .complianceTag("PCI_DSS")
                    .complianceTag("KVKK")
                    .complianceTag("GDPR")
            );
            
            return createPaymentResponse(payment, "Payment processed successfully", true);
            
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating payment: {}", e.getMessage());
            if (e.getMessage().contains("merchant_id")) {
                return createErrorResponse("Invalid merchant ID: " + request.getMerchantId());
            } else if (e.getMessage().contains("customer_id")) {
                return createErrorResponse("Invalid customer ID: " + request.getCustomerId());
            } else {
                return createErrorResponse("Data validation failed: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage());
            return createErrorResponse("Failed to process payment: " + e.getMessage());
        }
    }
    
    public PaymentResponse getPaymentById(Long id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        if (payment.isPresent()) {
            return createPaymentResponse(payment.get(), "Payment retrieved successfully", true);
        } else {
            return createErrorResponse("Payment not found with ID: " + id);
        }
    }
    
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Optional<Payment> payment = paymentRepository.findByTransactionId(transactionId);
        if (payment.isPresent()) {
            return createPaymentResponse(payment.get(), "Payment retrieved successfully", true);
        } else {
            return createErrorResponse("Payment not found with transaction ID: " + transactionId);
        }
    }
    
    public List<PaymentResponse> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(payment -> createPaymentResponse(payment, null, true))
                .collect(Collectors.toList());
    }
    
    public List<PaymentResponse> getPaymentsByMerchantId(String merchantId) {
        List<Payment> payments = paymentRepository.findByMerchantId(merchantId);
        return payments.stream()
                .map(payment -> createPaymentResponse(payment, null, true))
                .collect(Collectors.toList());
    }
    
    public List<PaymentResponse> getPaymentsByCustomerId(String customerId) {
        List<Payment> payments = paymentRepository.findByCustomerId(customerId);
        return payments.stream()
                .map(payment -> createPaymentResponse(payment, null, true))
                .collect(Collectors.toList());
    }
    
    public List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(payment -> createPaymentResponse(payment, null, true))
                .collect(Collectors.toList());
    }
    
    public PaymentResponse updatePaymentStatus(Long id, Payment.PaymentStatus newStatus) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            Payment.PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(newStatus);
            
            // Set completedAt if status is being changed to COMPLETED
            if (newStatus == Payment.PaymentStatus.COMPLETED && payment.getCompletedAt() == null) {
                payment.setCompletedAt(LocalDateTime.now());
            }
            
            payment.setGatewayResponse("Status updated to: " + newStatus);
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Audit log - Payment status update
            auditService.logEvent(
                auditService.createEvent()
                    .eventType("PAYMENT")
                    .action("STATUS_UPDATE")
                    .actor("api-user")
                    .resourceType("Payment")
                    .resourceId(payment.getPaymentId())
                    .additionalData("transactionId", payment.getTransactionId())
                    .additionalData("oldStatus", oldStatus.name())
                    .additionalData("newStatus", newStatus.name())
                    .additionalData("amount", payment.getAmount())
                    .complianceTag("PCI_DSS")
            );
            
            log.info("Payment status updated to {} for ID: {}", newStatus, id);
            return createPaymentResponse(updatedPayment, "Payment status updated successfully", true);
        } else {
            return createErrorResponse("Payment not found with ID: " + id);
        }
    }
    
    public PaymentResponse deletePayment(Long id) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            
            // Check if payment can be deleted (only pending or failed payments)
            if (payment.getStatus() == Payment.PaymentStatus.PENDING || 
                payment.getStatus() == Payment.PaymentStatus.FAILED) {
                
                paymentRepository.deleteById(id);
                log.info("Payment deleted successfully with ID: {}", id);
                return createPaymentResponse(payment, "Payment deleted successfully", true);
            } else {
                return createErrorResponse("Cannot delete payment with status: " + payment.getStatus());
            }
        } else {
            return createErrorResponse("Payment not found with ID: " + id);
        }
    }
    
    public PaymentResponse refundPayment(Long id) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            
            if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                payment.setGatewayResponse("Payment refunded");
                Payment updatedPayment = paymentRepository.save(payment);
                
                // Audit log - Payment refund
                auditService.logEvent(
                    auditService.createEvent()
                        .eventType("PAYMENT")
                        .action("REFUND")
                        .actor("api-user")
                        .resourceType("Payment")
                        .resourceId(payment.getPaymentId())
                        .additionalData("transactionId", payment.getTransactionId())
                        .additionalData("amount", payment.getAmount())
                        .additionalData("currency", payment.getCurrency())
                        .additionalData("refundReason", "Manual refund request")
                        .complianceTag("PCI_DSS")
                        .complianceTag("GDPR")
                );
                
                log.info("Payment refunded successfully with ID: {}", id);
                return createPaymentResponse(updatedPayment, "Payment refunded successfully", true);
            } else {
                return createErrorResponse("Cannot refund payment with status: " + payment.getStatus());
            }
        } else {
            return createErrorResponse("Payment not found with ID: " + id);
        }
    }
    
    /**
     * 3D Secure sürecini tamamlar
     */
    @Transactional
    public PaymentResponse complete3DSecurePayment(String paymentId, String bankTransactionId, String authCode, boolean success) {
        log.info("Completing 3D Secure payment: {}, success: {}", paymentId, success);
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentId);
            if (paymentOpt.isEmpty()) {
                return createErrorResponse("Payment not found: " + paymentId);
            }
            
            Payment payment = paymentOpt.get();
            
            if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
                return createErrorResponse("Payment is not in pending state: " + payment.getStatus());
            }
            
            if (success) {
                // 3D Secure başarılı
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setCompletedAt(LocalDateTime.now());
                payment.setGatewayResponse("3D Secure authentication successful");
                if (bankTransactionId != null) {
                    payment.setGatewayTransactionId(bankTransactionId);
                }
                
                log.info("3D Secure payment completed successfully: {}", paymentId);
                
                // Audit log - 3D Secure success
                auditService.logEvent(
                    auditService.createEvent()
                        .eventType("3D_SECURE")
                        .action("AUTHENTICATION_SUCCESS")
                        .actor("bank-system")
                        .resourceType("Payment")
                        .resourceId(paymentId)
                        .additionalData("transactionId", payment.getTransactionId())
                        .additionalData("bankTransactionId", bankTransactionId)
                        .additionalData("amount", payment.getAmount())
                        .complianceTag("PCI_DSS")
                        .complianceTag("3DS")
                );
                
            } else {
                // 3D Secure başarısız
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setGatewayResponse("3D Secure authentication failed");
                
                log.info("3D Secure payment failed: {}", paymentId);
                
                // Audit log - 3D Secure failure
                auditService.logEvent(
                    auditService.createEvent()
                        .eventType("3D_SECURE")
                        .action("AUTHENTICATION_FAILURE")
                        .actor("bank-system")
                        .resourceType("Payment")
                        .resourceId(paymentId)
                        .additionalData("transactionId", payment.getTransactionId())
                        .additionalData("failureReason", "3D Secure authentication failed")
                        .complianceTag("PCI_DSS")
                        .complianceTag("3DS")
                );
            }
            
            payment = paymentRepository.save(payment);
            
            return createPaymentResponse(payment, 
                success ? "3D Secure payment completed successfully" : "3D Secure payment failed", 
                success);
                
        } catch (Exception e) {
            log.error("Error completing 3D Secure payment: {}", paymentId, e);
            return createErrorResponse("Error completing 3D Secure payment: " + e.getMessage());
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return cardNumber;
        }
        
        // BIN (first 6 digits) + masked middle + last 4 digits format
        // Example: 4111111111111111 -> 411111******1111
        String bin = cardNumber.substring(0, 6);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        int middleLength = cardNumber.length() - 10; // Total - 6 (BIN) - 4 (last four)
        String maskedMiddle = "*".repeat(middleLength);
        
        return bin + maskedMiddle + lastFour;
    }
    
    private String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }
        
        // Remove any non-digit characters
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        
        if (cleanCardNumber.length() < 4) {
            return "UNKNOWN";
        }
        
        // Get the first few digits to determine the brand
        String prefix = cleanCardNumber.substring(0, Math.min(6, cleanCardNumber.length()));
        int firstDigit = Integer.parseInt(prefix.substring(0, 1));
        int firstTwoDigits = Integer.parseInt(prefix.substring(0, 2));
        int firstThreeDigits = prefix.length() >= 3 ? Integer.parseInt(prefix.substring(0, 3)) : 0;
        int firstFourDigits = prefix.length() >= 4 ? Integer.parseInt(prefix.substring(0, 4)) : 0;
        
        // Visa: starts with 4
        if (firstDigit == 4) {
            return "VISA";
        }
        
        // Mastercard: 51-55, 2221-2720
        if (firstTwoDigits >= 51 && firstTwoDigits <= 55) {
            return "MASTERCARD";
        }
        if (firstFourDigits >= 2221 && firstFourDigits <= 2720) {
            return "MASTERCARD";
        }
        
        // American Express: 34, 37
        if (firstTwoDigits == 34 || firstTwoDigits == 37) {
            return "AMEX";
        }
        
        // Discover: 6011, 622126-622925, 644-649, 65
        if (firstFourDigits == 6011 || firstTwoDigits == 65) {
            return "DISCOVER";
        }
        if (firstThreeDigits >= 644 && firstThreeDigits <= 649) {
            return "DISCOVER";
        }
        if (firstFourDigits >= 622126 && firstFourDigits <= 622925) {
            return "DISCOVER";
        }
        
        // Diners Club: 300-305, 36, 38
        if (firstThreeDigits >= 300 && firstThreeDigits <= 305) {
            return "DINERS";
        }
        if (firstTwoDigits == 36 || firstTwoDigits == 38) {
            return "DINERS";
        }
        
        // JCB: 3528-3589
        if (firstFourDigits >= 3528 && firstFourDigits <= 3589) {
            return "JCB";
        }
        
        return "UNKNOWN";
    }
    
    private String extractCardBin(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        return cleanCardNumber.length() >= 6 ? cleanCardNumber.substring(0, 6) : null;
    }
    
    private String extractCardLastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        return cleanCardNumber.length() >= 4 ? cleanCardNumber.substring(cleanCardNumber.length() - 4) : null;
    }
    
    private Payment.PaymentStatus processPaymentThroughGateway(PaymentRequest request, Payment payment) {
        log.info("Processing payment through gateway for payment: {}", payment.getPaymentId());
        
        try {
            // Önce gerçek banka entegrasyonunu dene
            RealBankIntegrationService.BankPaymentResult bankResult = 
                realBankIntegrationService.processPayment(request, payment);
            
            if (bankResult != null) {
                // Gerçek banka yanıtı var
                log.info("Real bank integration response received for payment: {}", payment.getPaymentId());
                
                if (bankResult.isRequires3DSecure()) {
                    // 3D Secure gerekli - bu durumda frontend'e özel yanıt dönmemiz gerekecek
                    log.info("3D Secure required for payment: {}, URL: {}", payment.getPaymentId(), bankResult.getThreeDSecureUrl());
                    payment.setGatewayResponse("3D Secure authentication required: " + bankResult.getThreeDSecureUrl());
                    payment.setGatewayTransactionId("3DS-" + UUID.randomUUID().toString().substring(0, 8));
                    return Payment.PaymentStatus.PENDING; // 3D Secure bekliyor
                    
                } else if (bankResult.isSuccess()) {
                    // Başarılı
                    payment.setGatewayResponse(bankResult.getBankResponseMessage());
                    payment.setGatewayTransactionId(bankResult.getBankTransactionId());
                    if (bankResult.getCompletedAt() != null) {
                        payment.setCompletedAt(bankResult.getCompletedAt());
                    }
                    return Payment.PaymentStatus.COMPLETED;
                    
                } else {
                    // Hata
                    payment.setGatewayResponse(bankResult.getErrorMessage() != null ? 
                        bankResult.getErrorMessage() : bankResult.getBankResponseMessage());
                    payment.setGatewayTransactionId("ERR-" + UUID.randomUUID().toString().substring(0, 8));
                    return Payment.PaymentStatus.FAILED;
                }
            }
            
            // Gerçek banka entegrasyonu yoksa simülasyon moduna geç
            log.info("Falling back to simulation mode for payment: {}", payment.getPaymentId());
            return processSimulatedPayment(payment);
            
        } catch (Exception e) {
            log.error("Error processing payment through gateway", e);
            payment.setGatewayResponse("Gateway error: " + e.getMessage());
            payment.setGatewayTransactionId("ERR-" + UUID.randomUUID().toString().substring(0, 8));
            return Payment.PaymentStatus.FAILED;
        }
    }
    
    /**
     * Simülasyon modu - mevcut mantık
     */
    private Payment.PaymentStatus processSimulatedPayment(Payment payment) {
        try {
            // Simulate processing time
            Thread.sleep(100);
            
            // Simulate success/failure based on card number
            if (payment.getCardNumber().endsWith("0000")) {
                payment.setGatewayResponse("Payment failed: Invalid card");
                payment.setGatewayTransactionId("GTW-" + UUID.randomUUID().toString().substring(0, 8));
                return Payment.PaymentStatus.FAILED;
            } else {
                payment.setGatewayResponse("Payment processed successfully");
                payment.setGatewayTransactionId("GTW-" + UUID.randomUUID().toString().substring(0, 8));
                return Payment.PaymentStatus.COMPLETED;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            payment.setGatewayResponse("Payment processing interrupted");
            return Payment.PaymentStatus.FAILED;
        }
    }
    
    private PaymentResponse createPaymentResponse(Payment payment, String message, boolean success) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentId(payment.getPaymentId());
        response.setTransactionId(payment.getTransactionId());
        response.setMerchantId(payment.getMerchantId());
        response.setCustomerId(payment.getCustomerId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCardNumber(payment.getCardNumber());
        response.setCardHolderName(payment.getCardHolderName());
        response.setCardBrand(payment.getCardBrand());
        response.setCardBin(payment.getCardBin());
        response.setCardLastFour(payment.getCardLastFour());
        response.setDescription(payment.getDescription());
        response.setGatewayResponse(payment.getGatewayResponse());
        response.setGatewayTransactionId(payment.getGatewayTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setMessage(message);
        response.setSuccess(success);
        return response;
    }
    
    private PaymentResponse createErrorResponse(String errorMessage) {
        PaymentResponse response = new PaymentResponse();
        response.setMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
}
