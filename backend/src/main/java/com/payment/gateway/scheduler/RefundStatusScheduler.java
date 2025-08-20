package com.payment.gateway.scheduler;

import com.payment.gateway.model.Refund;
import com.payment.gateway.repository.RefundRepository;
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
    
    /**
     * Her 30 saniyede bir PROCESSING durumundaki refund'ları kontrol et
     * 2 dakikadan eski olanları otomatik olarak COMPLETED yap
     */
    @Scheduled(fixedRate = 30000) // 30 saniye
    @Transactional
    public void processRefundCompletions() {
        log.debug("Starting scheduled refund completion processing");
        
        try {
            // 2 dakikadan eski PROCESSING refund'ları bul
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(2);
            
            List<Refund> processingRefunds = refundRepository.findByStatusAndCreatedAtBefore(
                Refund.RefundStatus.PROCESSING, cutoffTime);
            
            if (!processingRefunds.isEmpty()) {
                log.info("Found {} processing refunds older than 2 minutes, completing them", 
                    processingRefunds.size());
                
                for (Refund refund : processingRefunds) {
                    // Simüle edilmiş banka onayı
                    refund.setStatus(Refund.RefundStatus.COMPLETED);
                    refund.setGatewayResponse("Refund processed successfully");
                    refund.setUpdatedAt(LocalDateTime.now());
                    
                    refundRepository.save(refund);
                    
                    log.info("Refund {} automatically completed", refund.getRefundId());
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing refund completions: {}", e.getMessage());
        }
        
        log.debug("Completed scheduled refund completion processing");
    }
}
