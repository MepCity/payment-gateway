package com.payment.gateway.scheduler;

import com.payment.gateway.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookRetryScheduler {
    
    private final WebhookService webhookService;
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processWebhookRetries() {
        try {
            log.debug("Starting scheduled webhook retry processing");
            webhookService.processRetries();
            log.debug("Completed scheduled webhook retry processing");
        } catch (Exception e) {
            log.error("Error in scheduled webhook retry processing: {}", e.getMessage(), e);
        }
    }
}
