package com.payment.gateway.scheduler;

import com.payment.gateway.model.Refund;
import com.payment.gateway.model.Dispute;
import com.payment.gateway.repository.RefundRepository;
import com.payment.gateway.repository.DisputeRepository;
import com.payment.gateway.service.DisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundStatusScheduler {

    private final RefundRepository refundRepository;
    private final DisputeRepository disputeRepository;
    private final DisputeService disputeService;

    /**
     * Her 5 dakikada bir dispute deadline'larını kontrol eder
     * Süresi dolmuş merchant response'ları otomatik olarak accept eder
     */
    @Scheduled(fixedRate = 300000) // 5 dakika
    @Transactional
    public void checkDisputeDeadlines() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Merchant response deadline'ı geçmiş dispute'ları bul
            List<Dispute> expiredDisputes = disputeRepository.findDisputesWithExpiredResponseDeadline(now);
            
            log.info("Found {} disputes with expired merchant response deadline", expiredDisputes.size());
            
            for (Dispute dispute : expiredDisputes) {
                try {
                    log.info("Auto-accepting dispute {} due to expired merchant response deadline", 
                            dispute.getBankDisputeId());
                    
                    disputeService.autoAcceptDisputeDueToTimeout(dispute.getId());
                    
                } catch (Exception e) {
                    log.error("Error auto-accepting dispute {}: {}", 
                            dispute.getBankDisputeId(), e.getMessage(), e);
                }
            }
            
            // Admin evaluation deadline'ı geçmiş dispute'ları bul
            List<Dispute> expiredAdminDisputes = disputeRepository.findDisputesWithExpiredAdminDeadline(now);
            
            log.info("Found {} disputes with expired admin evaluation deadline", expiredAdminDisputes.size());
            
            for (Dispute dispute : expiredAdminDisputes) {
                try {
                    log.info("Auto-accepting dispute {} due to expired admin evaluation deadline", 
                            dispute.getBankDisputeId());
                    
                    disputeService.autoAcceptDisputeDueToAdminTimeout(dispute.getId());
                    
                } catch (Exception e) {
                    log.error("Error auto-accepting dispute due to admin timeout {}: {}", 
                            dispute.getBankDisputeId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error in dispute deadline check: {}", e.getMessage(), e);
        }
    }

    /**
     * Her gün gece yarısı dispute istatistiklerini günceller
     */
    @Scheduled(cron = "0 0 0 * * *") // Her gün gece yarısı
    @Transactional(readOnly = true)
    public void generateDisputeStatistics() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastWeek = now.minusDays(7);
            LocalDateTime lastMonth = now.minusDays(30);
            
            long weeklyDisputes = disputeRepository.countDisputesByDateRange(lastWeek, now);
            long monthlyDisputes = disputeRepository.countDisputesByDateRange(lastMonth, now);
            
            log.info("Dispute Statistics - Weekly: {}, Monthly: {}", weeklyDisputes, monthlyDisputes);
            
            // Burada istatistikleri veritabanına kaydedebilir veya external service'e gönderebiliriz
            
        } catch (Exception e) {
            log.error("Error generating dispute statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * Her 10 dakikada bir pending refund'ları kontrol eder
     * Uzun süredir pending olan refund'ları işleme alır
     */
    @Scheduled(fixedRate = 600000) // 10 dakika
    @Transactional
    public void checkPendingRefunds() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2); // 2 saatten eski
            
            List<Refund> longPendingRefunds = refundRepository.findLongPendingRefunds(cutoffTime);
            
            log.info("Found {} long pending refunds", longPendingRefunds.size());
            
            for (Refund refund : longPendingRefunds) {
                try {
                    log.info("Processing long pending refund: {}", refund.getId());
                    
                    // Burada refund'ı tekrar process edebilir veya alert gönderebiliriz
                    // refundService.reprocessRefund(refund.getId());
                    
                } catch (Exception e) {
                    log.error("Error processing long pending refund {}: {}", 
                            refund.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking pending refunds: {}", e.getMessage(), e);
        }
    }

    /**
     * Her saat başı webhook retry işlemlerini kontrol eder
     */
    @Scheduled(cron = "0 0 * * * *") // Her saat başı
    @Transactional(readOnly = true)
    public void logSchedulerStatus() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            long pendingDisputes = disputeRepository.countPendingMerchantResponses();
            long pendingAdminReviews = disputeRepository.countPendingAdminEvaluations();
            
            log.info("Scheduler Status - Pending Merchant Responses: {}, Pending Admin Reviews: {}", 
                    pendingDisputes, pendingAdminReviews);
            
        } catch (Exception e) {
            log.error("Error logging scheduler status: {}", e.getMessage(), e);
        }
    }
}
