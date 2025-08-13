package com.payment.gateway.service;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.model.VelocityCheck;
import com.payment.gateway.repository.VelocityCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.payment.gateway.service.AuditService;
import com.payment.gateway.model.AuditLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VelocityCheckService {
    
    private final VelocityCheckRepository velocityCheckRepository;
    private final AuditService auditService;
    
    // Configurable limits
    @Value("${app.fraud.velocity.card.transactions.per.minute:5}")
    private int cardTransactionsPerMinute;
    
    @Value("${app.fraud.velocity.card.transactions.per.hour:20}")
    private int cardTransactionsPerHour;
    
    @Value("${app.fraud.velocity.card.transactions.per.day:100}")
    private int cardTransactionsPerDay;
    
    @Value("${app.fraud.velocity.card.amount.per.hour:10000}")
    private BigDecimal cardAmountPerHour;
    
    @Value("${app.fraud.velocity.card.amount.per.day:50000}")
    private BigDecimal cardAmountPerDay;
    
    @Value("${app.fraud.velocity.ip.transactions.per.minute:10}")
    private int ipTransactionsPerMinute;
    
    @Value("${app.fraud.velocity.ip.transactions.per.hour:50}")
    private int ipTransactionsPerHour;
    
    @Value("${app.fraud.velocity.customer.transactions.per.hour:30}")
    private int customerTransactionsPerHour;
    
    @Value("${app.fraud.velocity.merchant.transactions.per.minute:100}")
    private int merchantTransactionsPerMinute;
    
    @Transactional
    public boolean checkVelocityLimits(PaymentRequest request, String ipAddress) {
        log.debug("Checking velocity limits for card: {}, IP: {}", 
                maskCardNumber(request.getCardNumber()), ipAddress);
        
        boolean limitExceeded = false;
        
        // Check card-based velocity limits
        limitExceeded |= checkCardVelocity(request);
        
        // Check IP-based velocity limits
        if (ipAddress != null) {
            limitExceeded |= checkIpVelocity(ipAddress);
        }
        
        // Check customer-based velocity limits
        limitExceeded |= checkCustomerVelocity(request.getCustomerId());
        
        // Check merchant-based velocity limits
        limitExceeded |= checkMerchantVelocity(request.getMerchantId());
        
        // Audit logging for velocity check
        auditService.createEvent()
            .eventType("VELOCITY_CHECK_PERFORMED")
            .severity(limitExceeded ? AuditLog.Severity.HIGH : AuditLog.Severity.LOW)
            .actor("fraud-engine")
            .action("CHECK")
            .resourceType("PAYMENT")
            .resourceId("VEL-" + UUID.randomUUID().toString().substring(0, 8))
            .additionalData("limitExceeded", limitExceeded)
            .additionalData("cardNumber", maskCardNumber(request.getCardNumber()))
            .additionalData("ipAddress", ipAddress)
            .additionalData("customerId", request.getCustomerId())
            .additionalData("merchantId", request.getMerchantId())
            .complianceTag("PCI_DSS")
            .log();
        
        log.info("Velocity check result for payment - Card: {}, Limit exceeded: {}", 
                maskCardNumber(request.getCardNumber()), limitExceeded);
        
        return limitExceeded;
    }
    
    private boolean checkCardVelocity(PaymentRequest request) {
        String cardPrefix = getCardPrefix(request.getCardNumber());
        boolean limitExceeded = false;
        
        // Check transactions per minute
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long transactionsInMinute = velocityCheckRepository.countCardTransactions(
                cardPrefix, oneMinuteAgo, LocalDateTime.now());
        
        if (transactionsInMinute >= cardTransactionsPerMinute) {
            saveVelocityCheck(VelocityCheck.VelocityType.CARD_TRANSACTIONS_PER_MINUTE, 
                            cardPrefix, null, (int) transactionsInMinute, BigDecimal.ZERO, 
                            oneMinuteAgo, LocalDateTime.now(), true, cardTransactionsPerMinute, null);
            limitExceeded = true;
            log.warn("Card velocity limit exceeded - transactions per minute: {} >= {}", 
                    transactionsInMinute, cardTransactionsPerMinute);
        }
        
        // Check transactions per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long transactionsInHour = velocityCheckRepository.countCardTransactions(
                cardPrefix, oneHourAgo, LocalDateTime.now());
        
        if (transactionsInHour >= cardTransactionsPerHour) {
            saveVelocityCheck(VelocityCheck.VelocityType.CARD_TRANSACTIONS_PER_HOUR, 
                            cardPrefix, null, (int) transactionsInHour, BigDecimal.ZERO, 
                            oneHourAgo, LocalDateTime.now(), true, cardTransactionsPerHour, null);
            limitExceeded = true;
            log.warn("Card velocity limit exceeded - transactions per hour: {} >= {}", 
                    transactionsInHour, cardTransactionsPerHour);
        }
        
        // Check amount per hour
        BigDecimal amountInHour = velocityCheckRepository.sumCardTransactionAmount(
                cardPrefix, oneHourAgo, LocalDateTime.now());
        
        if (amountInHour != null && amountInHour.compareTo(cardAmountPerHour) >= 0) {
            saveVelocityCheck(VelocityCheck.VelocityType.CARD_AMOUNT_PER_HOUR, 
                            cardPrefix, null, 0, amountInHour, 
                            oneHourAgo, LocalDateTime.now(), true, null, cardAmountPerHour);
            limitExceeded = true;
            log.warn("Card velocity limit exceeded - amount per hour: {} >= {}", 
                    amountInHour, cardAmountPerHour);
        }
        
        // Check transactions per day
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long transactionsInDay = velocityCheckRepository.countCardTransactions(
                cardPrefix, oneDayAgo, LocalDateTime.now());
        
        if (transactionsInDay >= cardTransactionsPerDay) {
            saveVelocityCheck(VelocityCheck.VelocityType.CARD_TRANSACTIONS_PER_DAY, 
                            cardPrefix, null, (int) transactionsInDay, BigDecimal.ZERO, 
                            oneDayAgo, LocalDateTime.now(), true, cardTransactionsPerDay, null);
            limitExceeded = true;
            log.warn("Card velocity limit exceeded - transactions per day: {} >= {}", 
                    transactionsInDay, cardTransactionsPerDay);
        }
        
        return limitExceeded;
    }
    
    private boolean checkIpVelocity(String ipAddress) {
        boolean limitExceeded = false;
        
        // Check IP transactions per minute
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        // Note: This would require storing IP in payments table or separate tracking
        // For now, we'll create a placeholder check
        
        saveVelocityCheck(VelocityCheck.VelocityType.IP_TRANSACTIONS_PER_MINUTE, 
                        ipAddress, null, 0, BigDecimal.ZERO, 
                        oneMinuteAgo, LocalDateTime.now(), false, ipTransactionsPerMinute, null);
        
        return limitExceeded;
    }
    
    private boolean checkCustomerVelocity(String customerId) {
        boolean limitExceeded = false;
        
        // Check customer transactions per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long transactionsInHour = velocityCheckRepository.countCustomerTransactions(
                customerId, oneHourAgo, LocalDateTime.now());
        
        if (transactionsInHour >= customerTransactionsPerHour) {
            saveVelocityCheck(VelocityCheck.VelocityType.CUSTOMER_TRANSACTIONS_PER_HOUR, 
                            customerId, null, (int) transactionsInHour, BigDecimal.ZERO, 
                            oneHourAgo, LocalDateTime.now(), true, customerTransactionsPerHour, null);
            limitExceeded = true;
            log.warn("Customer velocity limit exceeded - transactions per hour: {} >= {}", 
                    transactionsInHour, customerTransactionsPerHour);
        }
        
        return limitExceeded;
    }
    
    private boolean checkMerchantVelocity(String merchantId) {
        boolean limitExceeded = false;
        
        // Check merchant transactions per minute
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long transactionsInMinute = velocityCheckRepository.countMerchantTransactions(
                merchantId, oneMinuteAgo, LocalDateTime.now());
        
        if (transactionsInMinute >= merchantTransactionsPerMinute) {
            saveVelocityCheck(VelocityCheck.VelocityType.MERCHANT_TRANSACTIONS_PER_MINUTE, 
                            merchantId, merchantId, (int) transactionsInMinute, BigDecimal.ZERO, 
                            oneMinuteAgo, LocalDateTime.now(), true, merchantTransactionsPerMinute, null);
            limitExceeded = true;
            log.warn("Merchant velocity limit exceeded - transactions per minute: {} >= {}", 
                    transactionsInMinute, merchantTransactionsPerMinute);
        }
        
        return limitExceeded;
    }
    
    private void saveVelocityCheck(VelocityCheck.VelocityType type, String identifier, String merchantId,
                                  int transactionCount, BigDecimal totalAmount,
                                  LocalDateTime windowStart, LocalDateTime windowEnd,
                                  boolean limitExceeded, Integer allowedCount, BigDecimal allowedAmount) {
        
        VelocityCheck check = new VelocityCheck();
        check.setCheckId(generateCheckId());
        check.setType(type);
        check.setIdentifier(identifier);
        check.setMerchantId(merchantId);
        check.setTransactionCount(transactionCount);
        check.setTotalAmount(totalAmount);
        check.setWindowStart(windowStart);
        check.setWindowEnd(windowEnd);
        check.setLimitExceeded(limitExceeded);
        check.setAllowedCount(allowedCount);
        check.setAllowedAmount(allowedAmount);
        
        if (limitExceeded) {
            check.setDetails(String.format("Limit exceeded: %d/%d transactions or %s/%s amount", 
                    transactionCount, allowedCount, totalAmount, allowedAmount));
        }
        
        velocityCheckRepository.save(check);
    }
    
    private String getCardPrefix(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return "UNKNOWN";
        }
        // Use first 6 digits + last 4 for identification while maintaining privacy
        String first6 = cardNumber.substring(0, 6);
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return first6 + "****" + last4;
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }
    
    private String generateCheckId() {
        return "VEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public VelocityCheck getLatestCheckForCard(String cardNumber, VelocityCheck.VelocityType type) {
        String cardPrefix = getCardPrefix(cardNumber);
        LocalDateTime since = LocalDateTime.now().minusHours(24); // Look back 24 hours
        
        List<VelocityCheck> checks = velocityCheckRepository.findRecentChecks(cardPrefix, type, since);
        return checks.isEmpty() ? null : checks.get(0);
    }
}
